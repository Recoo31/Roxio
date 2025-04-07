package kurd.reco.core.data

import androidx.annotation.Keep

@Keep data class UpdateResponse(
    val versions: Versions,
    val downloads: Downloads,
    val changeLog: List<String>
) {
    @Keep data class Versions(
        val tv: Double,
        val mobile: Double,
    )
    @Keep data class Downloads(
        val tv: String,
        val mobile: String,
    )
}
