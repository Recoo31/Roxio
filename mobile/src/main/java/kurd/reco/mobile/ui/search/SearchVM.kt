package kurd.reco.mobile.ui.search

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurd.reco.core.AppLog
import kurd.reco.core.Global.clickedItem
import kurd.reco.core.ResourceState
import kurd.reco.core.api.Cache.checkCache
import kurd.reco.core.api.Cache.saveToCache
import kurd.reco.core.api.Resource
import kurd.reco.core.api.model.HomeItemModel
import kurd.reco.core.api.model.PlayDataModel
import kurd.reco.core.plugin.PluginManager

enum class FilterType {
    MOVIES, SERIES, BOTH
}

class SearchVM(private val pluginManager: PluginManager): ViewModel() {
    private val TAG = "SearchVM"

    val searchFieldState = TextFieldState()
    val clickedItem = ResourceState<PlayDataModel>(Resource.Loading)

    @OptIn(FlowPreview::class)
    val searchTextState = snapshotFlow { searchFieldState.text }
        .debounce(600)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(2000), "")

    var lastSearchedText: String = ""
        private set

    var searchList by mutableStateOf(emptyList<HomeItemModel>())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    var filterType by mutableStateOf(FilterType.BOTH)

    fun search(query: String) {
        _isLoading.value = true
        lastSearchedText = query

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = pluginManager.getSelectedPlugin().search(query)
                searchList = response
            } catch (t: Throwable) {
                t.printStackTrace()
            } finally {
                _isLoading.value = false
            }
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
}