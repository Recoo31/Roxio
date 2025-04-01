package kurd.reco.core.api.model

import kurd.reco.core.data.ItemDirection

data class HomeScreenModel(
    val title: String,
    val contents: List<HomeItemModel>,
    val id: String? = null,
    val itemDirection: ItemDirection = ItemDirection.Vertical
)

data class HomeItemModel(
    val id: Any,
    val title: String?,
    val poster: String,
    val isSeries: Boolean,
    val isLiveTv: Boolean
)