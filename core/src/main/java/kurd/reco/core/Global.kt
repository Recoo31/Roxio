package kurd.reco.core

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kurd.reco.core.Global.isDebugMode
import kurd.reco.core.api.model.DetailScreenModel
import kurd.reco.core.api.model.HomeItemModel
import kurd.reco.core.api.model.HomeScreenModel
import kurd.reco.core.api.model.PlayDataModel
import kurd.reco.core.data.ErrorModel
import kurd.reco.core.data.db.plugin.Plugin
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.system.exitProcess

object Global {
    var fetchRetryCount = 0
    var loginTryCount = 0

    var pluginLoaded by mutableStateOf(false)
    var currentPlugin by mutableStateOf<Plugin?>(null)
    var showPluginDialog by mutableStateOf(false)

    var fetchForPlayer by mutableStateOf(false)
    var playDataModel by mutableStateOf<PlayDataModel?>(null)
    var lastDetailItem by mutableStateOf<DetailScreenModel?>(null)
    var clickedItem by mutableStateOf<HomeItemModel?>(null)
    var clickedItemRow by mutableStateOf<HomeScreenModel?>(null)

    val isDebugMode = BuildConfig.DEBUG

    var errorModel by mutableStateOf(ErrorModel("", false))
}

object User {
    var accessToken by mutableStateOf<String?>(null)
}

object HttpClient {
    val httpClient = OkHttpClient.Builder().addInterceptor { chain ->
        val request: Request = chain.request()
        val proxyHost = System.getProperty("http.proxyHost")

        if (!isDebugMode && (proxyHost != null && proxyHost.isNotEmpty() || isVpnDetectedSimple())) {
            exitProcess(0)
        }

        chain.proceed(request)
    }.build()
}

object SGCheck {
    init {
        System.loadLibrary("core")
    }
    external fun getSG(context: Context): String
    external fun checkSGIntegrity(): Array<String>?
}