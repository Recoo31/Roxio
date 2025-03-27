package kurd.reco.core.api.model

import kurd.reco.core.data.ItemDirection

data class DiscoverCategory(
    val id: String,
    val name: String,
    val subCategories: List<DiscoverSubCategory> = emptyList()
)

data class DiscoverSubCategory(
    val id: String,
    val name: String
)

data class DiscoverItemsResponse(
    val items: List<HomeItemModel>,
    val hasMore: Boolean,
    val nextPage: Int? = null,
    val itemDirection: ItemDirection = ItemDirection.Vertical
)

///

data class Discover(
    val categoryId: String,
    val subCategoryId: String? = null,
    val page: Int = 1,
    val filter: String? = null
)

///

data class DiscoverFilter(
    val id: String,
    val name: String
)