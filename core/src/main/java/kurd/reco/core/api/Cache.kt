package kurd.reco.core.api

import kurd.reco.core.AppLog
import kurd.reco.core.api.model.PlayDataModel
import kurd.reco.core.api.model.CacheDataModelRoot

object Cache {

    suspend fun checkCache(id: String): CacheDataModelRoot {
        val url = "https://recoo.online/cache/api/getcache/$id"
        return try {
            app.get(url, timeout = 2L).parsed<CacheDataModelRoot>()
        } catch (t: Throwable) {
            t.printStackTrace()
            CacheDataModelRoot(false, null)
        }
    }

    suspend fun saveToCache(id: String, playData: PlayDataModel) {
        val url = "https://recoo.online/cache/api/setcache"
        val requestData = mapOf(
            "id" to id,
            "urls" to playData.urls,
            "title" to playData.title,
            "drm" to playData.drm,
            "subtitles" to playData.subtitles,
            "stream_headers" to playData.streamHeaders
        )
        val response = app.post(url, json = requestData)
        AppLog.d("Cache", "saveToCache: $response")
    }

    suspend fun deleteCache(id: String) {
        val url = "https://recoo.online/cache/api/delete/$id"
        try {
            val response = app.get(url).parsed<CacheDataModelRoot>()
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