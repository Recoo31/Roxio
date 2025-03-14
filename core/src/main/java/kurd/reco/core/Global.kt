package kurd.reco.core

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kurd.reco.core.Global.isDebugMode
import kurd.reco.core.api.model.DetailScreenModel
import kurd.reco.core.api.model.HomeItemModel
import kurd.reco.core.api.model.HomeScreenModel
import kurd.reco.core.api.model.PlayDataModel
import kurd.reco.core.data.db.plugin.Plugin
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.system.exitProcess

object Global {
    var accessToken by mutableStateOf<String?>(null)

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
