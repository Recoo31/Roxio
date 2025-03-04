package kurd.reco.core.api.model

import com.fasterxml.jackson.annotation.JsonProperty

data class DeletedCache(
    val status: Boolean,
    val message: String,
)

data class CacheDataModelRoot(
    val status: Boolean,
    val data: CachePlayDataModel?,
)

data class UrlDataModel(
    val first: String,
    val second: String
)

data class CachePlayDataModel(
    val id: String,
    val urls: List<UrlDataModel>,
    val title: String?,
    val drm: DrmDataModel?,
    val subtitles: List<SubtitleDataModel>?,
    @JsonProperty("stream_headers")
    val streamHeaders: Map<String, String>?,

    val clearKey: String?,
) {
    fun convertPlayDataModel(): PlayDataModel {
        val urls = this.urls.map { urlDataModel ->
            Pair(urlDataModel.first, urlDataModel.second)
        }

        return PlayDataModel(
            id = this.id,
            urls = urls,
            title = this.title,
            drm = this.drm,
            subtitles = this.subtitles,
            streamHeaders = this.streamHeaders
        )
    }

}