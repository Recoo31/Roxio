package kurd.reco.core.api

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.lagradost.nicehttp.Requests
import com.lagradost.nicehttp.ResponseParser
import kurd.reco.core.api.ApiUtils.responseParser
import kurd.reco.core.isProxyDetected
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File
import java.net.InetSocketAddress
import java.net.Proxy
import kotlin.reflect.KClass

sealed class Resource<out T> {
    data class Success<out T>(val value: T) : Resource<T>()
    data class Failure(val error: String) : Resource<Nothing>()
    data object Loading : Resource<Nothing>()
}

var prx = Proxy(Proxy.Type.HTTP, InetSocketAddress("188.132.198.10", 8800))

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
    val app = Requests(responseParser = responseParser)

//    val app = if (!isProxyDetected()) {
//        Requests(
//            responseParser = responseParser
//        )
//    } else Requests()
}
val app = ApiUtils.app


