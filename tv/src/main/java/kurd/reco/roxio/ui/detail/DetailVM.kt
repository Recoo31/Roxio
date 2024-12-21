package kurd.reco.roxio.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kurd.reco.core.AppLog
import kurd.reco.core.ResourceState
import kurd.reco.core.api.Resource
import kurd.reco.core.api.model.DetailScreenModel
import kurd.reco.core.api.model.PlayDataModel
import kurd.reco.core.plugin.PluginManager

class DetailVM(private val pluginManager: PluginManager): ViewModel() {
    val item: ResourceState<DetailScreenModel> = ResourceState(Resource.Loading)

    val clickedItem: ResourceState<PlayDataModel> = ResourceState(Resource.Loading)

    val seriesList by lazy {
        pluginManager.getSelectedPlugin().seriesList
    }

    private val TAG = "DetailScreenVM"


    fun getMovie(id: Any, isSeries: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                AppLog.d(TAG, "getMovie: ID: $id | IsSeries: $isSeries")
                pluginManager.getSelectedPlugin().getDetailScreenItems(id, isSeries).also {
                    item.update(it)
                }
            } catch (t: Throwable) {
                item.handleError(t, TAG)
            }
        }
    }

    fun getUrl(id: Any, title: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                pluginManager.getSelectedPlugin().getUrl(id, title).also {
                    clickedItem.update(it)
                }
            } catch (t: Throwable) {
                clickedItem.handleError(t, TAG)
            }
        }
    }


    fun clearClickedItem() {
        clickedItem.update(Resource.Loading)
    }
}