package kurd.reco.core.api.model

data class PlayDataModel(
    val id: String,
    val urls: List<Pair<String, String>>,
    val title: String?,
    val drm: DrmDataModel?,
    val subtitles: List<SubtitleDataModel>?,
    val streamHeaders: Map<String, String>?,
)

data class DrmDataModel(
    val licenseUrl: String,
    val clearKey: String?,
    val headers: Map<String, String>?
)

data class SubtitleDataModel(
    val url: String,
    val language: String,
    val id: Any?
)