package kurd.reco.core.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kurd.reco.core.AppLog
import kurd.reco.core.ResourceState
import kurd.reco.core.api.Cache.checkCache
import kurd.reco.core.api.Cache.saveToCache
import kurd.reco.core.api.Resource
import kurd.reco.core.api.model.Discover
import kurd.reco.core.api.model.DiscoverCategory
import kurd.reco.core.api.model.DiscoverFilter
import kurd.reco.core.api.model.DiscoverSubCategory
import kurd.reco.core.api.model.HomeItemModel
import kurd.reco.core.api.model.PlayDataModel
import kurd.reco.core.data.ItemDirection
import kurd.reco.core.plugin.PluginManager

class DiscoverVM(private val pluginManager: PluginManager) : ViewModel() {
    private val TAG = "DiscoverVM"

    var categories by mutableStateOf<List<DiscoverCategory>>(emptyList())
    val discoverItems = ResourceState<List<HomeItemModel>>(Resource.Loading)
    val clickedItem = ResourceState<PlayDataModel>(Resource.Loading)

    var selectedCategory by mutableStateOf<DiscoverCategory?>(null)
    var selectedSubCategory by mutableStateOf<DiscoverSubCategory?>(null)
    var currentFilter by mutableStateOf<String?>(null)
    var isLoadingMore by mutableStateOf(false)
        private set
    var isLoading by mutableStateOf(false)
        private set
    
    private var currentPage = 1
    private var hasMore = false
    private var currentItems = emptyList<HomeItemModel>()
    var itemDirection by mutableStateOf(ItemDirection.Vertical)

    var discoverFilters by mutableStateOf<List<DiscoverFilter>>(emptyList())

    private var currentPluginId: String? = null

    private fun resetState() {
        categories = emptyList()
        selectedCategory = null
        selectedSubCategory = null
        currentFilter = null
        discoverFilters = emptyList()
        currentPage = 1
        hasMore = false
        currentItems = emptyList()
        discoverItems.setLoading()
    }

    private fun checkAndHandlePluginChange() {
        val plugin = pluginManager.getLastSelectedPlugin()
        val pluginId = plugin?.id
        
        // Only reset and reload if the plugin has changed
        if (pluginId != currentPluginId) {
            resetState()
            currentPluginId = pluginId
        }
    }

    fun loadCategories() {
        checkAndHandlePluginChange()
        try {
            categories = pluginManager.getSelectedPlugin().discoverCategories ?: emptyList()
        } catch (t: Throwable) {
            AppLog.e(TAG, "Error loading categories")
        }
    }

    fun loadFilters() {
        try {
            discoverFilters = pluginManager.getSelectedPlugin().discoverFilters ?: emptyList()
        } catch (t: Throwable) {
            AppLog.e(TAG, "Error loading filters")
        }
    }

    private fun loadDiscoverItems(categoryId: String, subCategoryId: String? = null, filter: String? = null, isLoadMore: Boolean = false) {
        // Check for plugin changes before loading items
        if (!isLoadMore) {
            checkAndHandlePluginChange()
        }
        
        if (isLoadMore && (!hasMore || isLoadingMore)) return

        viewModelScope.launch(Dispatchers.IO) {
            if (!isLoadMore) {
                discoverItems.setLoading()
                currentPage = 1
                currentItems = emptyList()
            }

            isLoadingMore = true

            discoverItems.update(
                runCatching {
                    val response = pluginManager.getSelectedPlugin().getDiscoverItems(
                        Discover(
                            categoryId = categoryId,
                            subCategoryId = subCategoryId,
                            page = currentPage,
                            filter = filter
                        )
                    )

                    when (response) {
                        is Resource.Success -> {
                            val data = response.value
                            val newItems = data.items
                            hasMore = data.hasMore
                            currentPage = data.nextPage ?: (currentPage + 1)
                            itemDirection = data.itemDirection

                            currentItems = if (isLoadMore) {
                                currentItems + newItems
                            } else {
                                newItems
                            }

                            // Update selected category and subcategory for new items
                            runBlocking {
                                loadCategories()
                                val currentCategoryId = selectedCategory?.id
                                val currentSubCategoryId = selectedSubCategory?.id
                                if (currentCategoryId != null) {
                                    selectedCategory = categories.find { it.id == currentCategoryId }

                                    if (currentSubCategoryId != null) {
                                        selectedSubCategory = selectedCategory?.subCategories?.find {
                                            it.id == currentSubCategoryId
                                        }
                                    }
                                }
                            }

                            Resource.Success(currentItems)
                        }
                        is Resource.Failure -> {
                            hasMore = false
                            response
                        }
                        Resource.Loading -> Resource.Loading
                    }
                }.getOrElse {
                    Resource.Failure(it.localizedMessage ?: "Unknown Error")
                }
            )

            isLoadingMore = false
        }
    }

    fun selectCategory(category: DiscoverCategory) {
        selectedCategory = category
        selectedSubCategory = null
        loadDiscoverItems(category.id)
    }

    fun selectSubCategory(subCategory: DiscoverSubCategory) {
        selectedSubCategory = subCategory
        selectedCategory?.let { category ->
            loadDiscoverItems(category.id, subCategory.id)
        }
    }

    fun loadMoreIfNeeded() {
        selectedCategory?.let { category ->
            loadDiscoverItems(
                categoryId = category.id,
                subCategoryId = selectedSubCategory?.id,
                isLoadMore = true,
                filter = currentFilter
            )
        }
    }

    fun applyFilter(filter: String) {
        currentFilter = filter
        selectedCategory?.let { category ->
            loadDiscoverItems(
                categoryId = category.id,
                subCategoryId = selectedSubCategory?.id,
                isLoadMore = false,
                filter = filter
            )
        }
    }

    fun getUrl(id: Any, title: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            val selectedPlugin = pluginManager.getSelectedPlugin()
            val useCache = selectedPlugin.useCache
            try {
                if (useCache) {
                    val resp = checkCache(id.toString())
                    if (resp.status) {
                        AppLog.d(TAG, "getUrl: Cache found")
                        clickedItem.setSuccess(resp.data!!.convertPlayDataModel())
                    } else {
                        AppLog.i(TAG, "getUrl: Cache not found")
                        selectedPlugin.getUrl(id, title).also {
                            clickedItem.update(it)
                            if (it is Resource.Success) {
                                saveToCache(id.toString(), it.value)
                            }
                        }
                    }
                } else {
                    AppLog.i(TAG, "getUrl: Doesn't using cache")
                    selectedPlugin.getUrl(id, title).also {
                        clickedItem.update(it)
                    }
                }
            } catch (t: Throwable) {
                clickedItem.handleError(t, TAG)
            }
        }
    }

    fun clearClickedItem() = clickedItem.update(Resource.Loading)
} 