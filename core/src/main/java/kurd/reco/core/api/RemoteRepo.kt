package kurd.reco.core.api

import androidx.annotation.Keep
import kurd.reco.core.api.model.DetailScreenModel
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
    fun getAccessToken(token: String)
    var seriesList: List<SeriesDataModel>?
    var pagerList: List<PagerDataClass>?
    var useCache: Boolean
    var categoryList: List<String>?
    var seeMore: Boolean
}