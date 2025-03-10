package kurd.reco.mobile.ui.home

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kurd.reco.core.AppLog
import kurd.reco.core.Global
import kurd.reco.core.Global.pluginLoaded
import kurd.reco.core.ResourceState
import kurd.reco.core.api.Cache.checkCache
import kurd.reco.core.api.Cache.saveToCache
import kurd.reco.core.api.Resource
import kurd.reco.core.api.model.HomeItemModel
import kurd.reco.core.api.model.HomeScreenModel
import kurd.reco.core.api.model.PagerDataClass
import kurd.reco.core.api.model.PlayDataModel
import kurd.reco.core.data.db.plugin.Plugin
import kurd.reco.core.plugin.PluginManager

class HomeVM(private val pluginManager: PluginManager) : ViewModel() {
    private val TAG = "HomeVM"
    private var currentJob: Job? = null

    val moviesList = ResourceState<List<HomeScreenModel>>(Resource.Loading)
    val clickedItem = ResourceState<PlayDataModel>(Resource.Loading)
    val selectedCategoryList = ResourceState<List<HomeItemModel>>(Resource.Loading)

    val selectedPlugin = mutableStateOf<Plugin?>(null)

    init {
        loadMovies()
    }

    fun loadMovies() {
        currentJob?.cancel()

        currentJob = viewModelScope.launch(Dispatchers.IO) {
            moviesList.setLoading()
            val result = runCatching {
                runCatching {
                    Global.accessToken?.let {
                        pluginManager.getSelectedPlugin().getAccessToken(it)
                    }
                }

                pluginManager.getSelectedPlugin().getHomeScreenItems()
            }.getOrElse {
                val message = it.localizedMessage
                if (message == "StandaloneCoroutine was cancelled") return@getOrElse Resource.Loading
                if (message != null) {
                    Resource.Failure(message)
                } else {
                    Global.showPluginDialog = true
                    Resource.Failure("Select Plugin")
                }
            }
            pluginLoaded = true
            moviesList.update(result)
        }
    }


    fun getCategoryItems(category: String) {
        viewModelScope.launch(Dispatchers.IO) {
            selectedCategoryList.setLoading()
            selectedCategoryList.update(
                runCatching {
                    pluginManager.getSelectedPlugin().getCategoryItems(category)
                }.getOrElse {
                    Resource.Failure(it.localizedMessage ?: "Unknown Error")
                }
            )
        }
    }

    fun getViewAll(id: Any) {
        viewModelScope.launch(Dispatchers.IO) {
            selectedCategoryList.setLoading()
            selectedCategoryList.update(
                runCatching {
                    pluginManager.getSelectedPlugin().getMore(id)
                }.getOrElse {
                    Resource.Failure(it.localizedMessage ?: "Unknown Error")
                }
            )
            println(selectedCategoryList.state.value)
        }
    }

    fun setViewAll(list: List<HomeItemModel>) {
        viewModelScope.launch(Dispatchers.IO) {
            selectedCategoryList.setSuccess(list)
        }
    }

    fun getUrl(id: Any, title: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            val useCache = pluginManager.getSelectedPlugin().useCache
            try {
                if (useCache) {
                    val resp = checkCache(id.toString())
                    if (resp.status) {
                        AppLog.d(TAG, "getUrl: Cache found")
                        clickedItem.setSuccess(resp.data!!.convertPlayDataModel())
                    } else {
                        AppLog.i(TAG, "getUrl: Cache not found")
                        pluginManager.getSelectedPlugin().getUrl(id, title).also {
                            clickedItem.update(it)
                            if (it is Resource.Success) {
                                saveToCache(id.toString(), it.value)
                            }
                        }
                    }
                } else {
                    AppLog.i(TAG, "getUrl: Doesn't using cache")
                    pluginManager.getSelectedPlugin().getUrl(id, title).also {
                        clickedItem.update(it)
                    }

                }
            } catch (t: Throwable) {
                clickedItem.handleError(t, TAG)
            }
        }
    }

    fun clearClickedItem() {
        clickedItem.update(Resource.Loading)
    }

    fun resetCategory() {
        selectedCategoryList.update(Resource.Loading)
    }

    fun resetMovies() {
        moviesList.update(Resource.Loading)
        pluginLoaded = false
    }

    fun getPagerList(): List<PagerDataClass>? =
        runCatching { pluginManager.getSelectedPlugin().pagerList }.getOrNull()

    fun getCategories(): List<String>? =
        runCatching { pluginManager.getSelectedPlugin().categoryList }.getOrNull()

    fun isThereSeeMore(): Boolean =
        runCatching { pluginManager.getSelectedPlugin().seeMore }.getOrDefault(false)
}
