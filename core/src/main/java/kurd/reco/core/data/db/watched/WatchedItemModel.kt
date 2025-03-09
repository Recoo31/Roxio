package kurd.reco.core.data.db.watched

import androidx.room.Entity
import androidx.room.PrimaryKey
import kurd.reco.core.api.model.HomeItemModel
import kurd.reco.core.api.model.HomeScreenModel
import androidx.room.TypeConverter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule

class Converters {

    private val objectMapper = ObjectMapper().registerModule(KotlinModule())

    // HomeScreenModel'i JSON string'e dönüştürme
    @TypeConverter
    fun fromHomeScreenModel(homeScreenModel: HomeScreenModel?): String? {
        return try {
            homeScreenModel?.let { objectMapper.writeValueAsString(it) }
        } catch (e: Exception) {
            null
        }
    }

    // JSON string'i HomeScreenModel objesine dönüştürme
    @TypeConverter
    fun toHomeScreenModel(data: String?): HomeScreenModel? {
        return try {
            data?.let { objectMapper.readValue(it, HomeScreenModel::class.java) }
        } catch (e: Exception) {
            null
        }
    }
}

@Entity(tableName = "watched_items")
data class WatchedItemModel(
    @PrimaryKey val id: String,
    val title: String?,
    val poster: String,
    val isSeries: Boolean,
    val resumePosition: Long,
    val totalDuration: Long,
    val pluginId: String,
    val itemsRow: HomeScreenModel? = null
) {
    fun toHomeItemModel() = HomeItemModel(
        id = id,
        title = title,
        poster = poster,
        isSeries = isSeries,
        isLiveTv = false
    )
}
