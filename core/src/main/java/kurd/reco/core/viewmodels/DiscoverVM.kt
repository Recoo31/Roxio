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
import kurd.reco.core.api.Resource
import kurd.reco.core.api.model.Discover
import kurd.reco.core.api.model.DiscoverCategory
import kurd.reco.core.api.model.DiscoverFilter
import kurd.reco.core.api.model.DiscoverSubCategory
import kurd.reco.core.api.model.HomeItemModel
import kurd.reco.core.data.ItemDirection
import kurd.reco.core.plugin.PluginManager

class DiscoverVM(private val pluginManager: PluginManager) : ViewModel() {
    private val TAG = "DiscoverVM"

    var categories by mutableStateOf<List<DiscoverCategory>>(emptyList())
    val discoverItems = ResourceState<List<HomeItemModel>>(Resource.Loading)

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

    fun loadCategories() {
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
                            hasMore = newItems.isNotEmpty()
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
} 