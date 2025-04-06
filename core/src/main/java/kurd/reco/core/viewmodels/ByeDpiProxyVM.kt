package kurd.reco.core.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kurd.reco.core.ByeDpiProxy


//thx https://github.com/dovecoteescapee/ByeDPIAndroid
class ByeDpiProxyVM : ViewModel() {
    private var proxy = ByeDpiProxy()
    private var proxyJob: Job? = null
    private val mutex = Mutex()

    companion object {
        private val TAG: String = ByeDpiProxyVM::class.java.simpleName
    }

    init {
        runBlocking {
            start()
        }
    }

    suspend fun start() {
        Log.i(TAG, "Starting")

        try {
            mutex.withLock {
                startProxy()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start proxy", e)
            stop()
        }
    }

    suspend fun stop() {
        Log.i(TAG, "Stopping VPN")

        mutex.withLock {
            stopProxy()
        }
    }

    private fun startProxy() {
        Log.i(TAG, "Starting proxy")

        if (proxyJob != null) {
            Log.w(TAG, "Proxy fields not null")
            throw IllegalStateException("Proxy fields not null")
        }

        proxy = ByeDpiProxy()

        proxyJob = viewModelScope.launch(Dispatchers.IO) {
            val code = proxy.startProxy()

            withContext(Dispatchers.Main) {
                if (code != 0) {
                    Log.e(TAG, "Proxy stopped with code $code")
                }
            }
        }

        Log.i(TAG, "Proxy started")
    }

    private suspend fun stopProxy() {
        Log.i(TAG, "Stopping proxy")

        proxy.stopProxy()
        proxyJob?.join()
        proxyJob = null

        Log.i(TAG, "Proxy stopped")
    }
}
