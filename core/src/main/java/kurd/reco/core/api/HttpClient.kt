package kurd.reco.core.api

import com.lagradost.nicehttp.addGenericDns
import kurd.reco.core.Global.isDebugMode
import kurd.reco.core.isVpnDetectedSimple
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.InetSocketAddress
import java.net.Proxy
import kotlin.system.exitProcess

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
