package kurd.reco.roxio.ui.search

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
import kurd.reco.core.api.model.SearchModel
import kurd.reco.core.plugin.PluginManager


enum class FilterType {
    MOVIES, SERIES, BOTH
}

class SearchVM(private val pluginManager: PluginManager) : ViewModel() {
    val searchFieldState = TextFieldState()

    @OptIn(FlowPreview::class)
    val searchTextState = snapshotFlow { searchFieldState.text }
        .debounce(1800)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(2000), "")

    var lastSearchedText: String = ""
        private set

    var searchList by mutableStateOf(emptyList<SearchModel>())

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
}