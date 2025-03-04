package kurd.reco.core.data

import androidx.annotation.Keep

@Keep data class VersionData(
    val version: Double,
    val downloadUrl: String,
    val changeLog: List<String>
)