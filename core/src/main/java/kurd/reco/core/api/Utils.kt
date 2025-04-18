package kurd.reco.core.api

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.lagradost.nicehttp.Requests
import com.lagradost.nicehttp.ResponseParser
import kotlinx.coroutines.runBlocking
import kurd.reco.core.EncryptionUtil
import kurd.reco.core.Global
import kurd.reco.core.Global.isDebugMode
import kurd.reco.core.SGCheck
import kurd.reco.core.api.ApiUtils.requests
import kurd.reco.core.api.ApiUtils.requestsWithDpi
import kurd.reco.core.api.HttpClient.httpClient
import kotlin.reflect.KClass

sealed class Resource<out T> {
    data class Success<out T>(val value: T) : Resource<T>()
    data class Failure(val error: String) : Resource<Nothing>()
    data object Loading : Resource<Nothing>()
}

data class GT(
    val cors: String,
    val api: String,
    val ip: String,
)


object ApiUtils {
    private val responseParser = object : ResponseParser {
        val mapper: ObjectMapper = jacksonObjectMapper().apply {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            enable(JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION)
            registerKotlinModule()
        }

        override fun <T : Any> parse(text: String, kClass: KClass<T>): T =
            mapper.readValue(text, kClass.java)

        override fun <T : Any> parseSafe(text: String, kClass: KClass<T>): T? =
            try {
                mapper.readValue(text, kClass.java)
            } catch (e: Exception) {
                null
            }

        override fun writeValueAsString(obj: Any): String =
            mapper.writeValueAsString(obj)
    }

    val requests: Requests by lazy {
        Requests(responseParser = responseParser, baseClient = httpClient())
    }
    val requestsWithDpi: Requests by lazy {
        Requests(responseParser = responseParser, baseClient = httpClient(true))
    }
}

val app = requests
val appWithDpi = requestsWithDpi // for plugins
val localApp = if (Global.platform == "mobile" && !isDebugMode) requestsWithDpi else requests // i dont know its working in tv

object Api {
    private var apiData: Array<String>? = null

    init {
        apiData = SGCheck.checkSGIntegrity()
    }

    private val GT_API = apiData?.get(0) ?: throw IllegalStateException("API not initialized")
    private val ENCRYPTED_PLUGIN_URL = apiData?.get(1) ?: throw IllegalStateException("API not initialized")

    private fun getGT(): String {
        return EncryptionUtil.decrypt(GT_API)
    }

    val response = runBlocking {
        app.get(url = getGT()).parsed<GT>()
    }

    private val ENCRYPTED_API_URL = response.api
    private val ENCRYPTED_IP = response.ip
    private val ENCRYPTED_CORS_URL = response.cors

    private fun getApiUrl(): String {
        return EncryptionUtil.decrypt(ENCRYPTED_API_URL)
    }

    private fun getIp(): String {
        return EncryptionUtil.decrypt(ENCRYPTED_IP)
    }

    private fun getPluginUrl(): String {
        return EncryptionUtil.decrypt(ENCRYPTED_PLUGIN_URL)
    }

    private fun getCors(): String {
        return EncryptionUtil.decrypt(ENCRYPTED_CORS_URL)
    }

    //val proxy = Proxy(Proxy.Type.SOCKS, InetSocketAddress(getIp(), 1080))

    val CORS_PROXY by lazy {
        getCors()
    }
    val API_URL = getApiUrl()
    val PLUGIN_URL = getPluginUrl()
    val ROXIO_API = if (Global.platform == "mobile") API_URL else "$CORS_PROXY/$API_URL"
}