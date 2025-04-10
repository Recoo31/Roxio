package kurd.reco.core.api

import kurd.reco.core.AppLog
import kurd.reco.core.User
import kurd.reco.core.api.Api.API_URL
import kurd.reco.core.api.Api.CORS_PROXY
import kurd.reco.core.api.Api.ROXIO_API
import kurd.reco.core.api.model.CacheDataModelRoot
import kurd.reco.core.api.model.DeletedCache
import kurd.reco.core.api.model.PlayDataModel

object Cache {
    private val headers = mapOf("Authorization" to "${User.accessToken}")

    suspend fun checkCache(id: String): CacheDataModelRoot {
        val url = "$ROXIO_API/cache/api/getcache/$id"
        return try {
            localApp.get(url, headers, timeout = 2L).parsed<CacheDataModelRoot>()
        } catch (t: Throwable) {
            t.printStackTrace()
            CacheDataModelRoot(false, null)
        }
    }

    suspend fun saveToCache(id: String, playData: PlayDataModel) {
        val url = "$ROXIO_API/cache/api/setcache"
        val requestData = mapOf(
            "id" to id,
            "urls" to playData.urls,
            "title" to playData.title,
            "drm" to playData.drm,
            "subtitles" to playData.subtitles,
            "stream_headers" to playData.streamHeaders
        )
        val response = localApp.post(url, headers, json = requestData)
        AppLog.d("Cache", "saveToCache: $response")
    }

    suspend fun deleteCache(id: String) {
        val url = "$ROXIO_API/cache/api/delete/$id"
        try {
            val response = localApp.get(url, headers).parsed<DeletedCache>()
            if (response.status) {
                AppLog.i("Cache", "Cache cache deleted | $id")
            } else {
                AppLog.e("Cache", "Cache cache cant deleted | $id |\n $response")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            AppLog.e("Cache", "Cache cache cant deleted | $id")
        }
    }
}