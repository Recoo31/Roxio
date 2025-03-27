package kurd.reco.core.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kurd.reco.core.AppLog
import kurd.reco.core.ResourceState
import kurd.reco.core.api.Resource
import kurd.reco.core.api.model.Discover
import kurd.reco.core.api.model.DiscoverCategory
import kurd.reco.core.api.model.DiscoverItemsResponse
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
    var isLoadingMore by mutableStateOf(false)
        private set
    
    private var currentPage = 1
    private var hasMore = false
    private var currentItems = emptyList<HomeItemModel>()
    var itemDirection by mutableStateOf(ItemDirection.Vertical)

    var discoverFilters by mutableStateOf(pluginManager.getSelectedPlugin().discoverFilters)

    init {
        loadCategories()
    }

    fun loadCategories() {
        categories = pluginManager.getSelectedPlugin().discoverCategories ?: emptyList()
    }

    fun reloadFilters() {
        discoverFilters = pluginManager.getSelectedPlugin().discoverFilters
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
                            hasMore = data.hasMore
                            currentPage = data.nextPage ?: (currentPage + 1)
                            itemDirection = data.itemDirection

                            currentItems = if (isLoadMore) {
                                currentItems + newItems
                            } else {
                                newItems
                            }

                            Resource.Success(currentItems)
                        }
                        is Resource.Failure -> response
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
                isLoadMore = true
            )
        }
    }

    fun applyFilter(filter: String) {
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