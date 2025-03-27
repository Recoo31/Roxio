package kurd.reco.core.api

import androidx.annotation.Keep
import kurd.reco.core.api.model.DetailScreenModel
import kurd.reco.core.api.model.Discover
import kurd.reco.core.api.model.DiscoverCategory
import kurd.reco.core.api.model.DiscoverFilter
import kurd.reco.core.api.model.DiscoverItemsResponse
import kurd.reco.core.api.model.HomeItemModel
import kurd.reco.core.api.model.HomeScreenModel
import kurd.reco.core.api.model.PagerDataClass
import kurd.reco.core.api.model.PlayDataModel
import kurd.reco.core.api.model.SearchModel
import kurd.reco.core.api.model.SeriesDataModel

@Keep interface RemoteRepo {
    suspend fun getHomeScreenItems(): Resource<List<HomeScreenModel>>
    suspend fun getCategoryItems(category: Any): Resource<List<HomeItemModel>>
    suspend fun getDetailScreenItems(id: Any, isSeries: Boolean): Resource<DetailScreenModel>
    suspend fun getUrl(id: Any, title: String?): Resource<PlayDataModel>
    suspend fun search(query: String): List<SearchModel>
    suspend fun getMore(id: Any): Resource<List<HomeItemModel>>
    fun getAccessToken(token: String)
    var seriesList: List<SeriesDataModel>?
    var pagerList: List<PagerDataClass>?
    var useCache: Boolean
    var categoryList: List<String>?
    var seeMore: Boolean

    // Discover related methods
    var discoverCategories: List<DiscoverCategory>?
    var discoverFilters: List<DiscoverFilter>?
    suspend fun getDiscoverItems(discover: Discover): Resource<DiscoverItemsResponse>
}