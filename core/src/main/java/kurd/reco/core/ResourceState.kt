package kurd.reco.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kurd.reco.core.api.Resource

class ResourceState<T>(initialValue: Resource<T>) {
    private val _state = MutableStateFlow(initialValue)
    val state: StateFlow<Resource<T>> = _state

    fun setLoading() {
        _state.value = Resource.Loading
    }

    fun setSuccess(data: T) {
        _state.value = Resource.Success(data)
    }

    fun setFailure(message: String) {
        _state.value = Resource.Failure(message)
    }

    fun update(newValue: Resource<T>) {
        _state.value = newValue
    }

    fun handleError(t: Throwable, tag: String) {
        t.printStackTrace()
        setFailure(t.localizedMessage ?: t.message ?: "Unknown Error")
        AppLog.d(tag, "Error: ${t.localizedMessage ?: t.message ?: "Unknown Error"}")
    }
}