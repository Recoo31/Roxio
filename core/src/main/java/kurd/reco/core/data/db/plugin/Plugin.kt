package kurd.reco.core.data.db.plugin

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plugins")
data class Plugin(
    @PrimaryKey val id: String,
    val name: String,
    val classPath: String,
    val className: String,
    val filePath: String,
    val version: String,
    val downloadUrl: String = "",
    val image: String? = null,
    val active: Boolean,
    val isSelected: Boolean = false
)

@Entity(tableName = "deleted_plugins")
data class DeletedPlugin(
    @PrimaryKey val id: String
)