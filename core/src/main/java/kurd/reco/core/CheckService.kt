package kurd.reco.core

import kurd.reco.core.api.app
import kotlinx.coroutines.withTimeout

suspend fun checkRpd(): Boolean {
    val rpdApi = "http://192.168.1.37:5000/admin"

    return try {
        withTimeout(2000L) {
            val response = app.get(rpdApi)
            response.isSuccessful
        }
    } catch (t: Throwable) {
        println("Timeout or other error occurred: ${t.message}")
        false
    }
}