package kurd.reco.core

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.lagradost.nicehttp.addGenericDns
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
import java.net.InetSocketAddress
import java.net.Proxy
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

    private fun OkHttpClient.Builder.addQuad9Dns() = (
            addGenericDns(
                "https://dns.quad9.net/dns-query",
                // https://www.quad9.net/service/service-addresses-and-features
                listOf(
                    "9.9.9.9",
                    "149.112.112.112",
                )
            ))

    fun httpClient(dpi: Boolean = false): OkHttpClient = OkHttpClient.Builder()
        .apply {
            if (dpi) {
                proxy(Proxy(Proxy.Type.SOCKS, InetSocketAddress("127.0.0.1", 8118)))
                addQuad9Dns()
            }
        }
        .addInterceptor { chain ->
            val request: Request = chain.request()
            val proxyHost = System.getProperty("http.proxyHost")

            if (!isDebugMode && (proxyHost != null && proxyHost.isNotEmpty() || isVpnDetectedSimple())) {
                exitProcess(0)
            }
            
            return@addInterceptor try {
                chain.proceed(request)
            } catch (t: Exception) {
                t.printStackTrace()
                chain.proceed(request)
            }
        }.build()
}

object SGCheck {
    init {
        System.loadLibrary("core")
    }
    external fun getSG(context: Context): String
    external fun checkSGIntegrity(): Array<String>?
}