package kurd.reco.core.data.db.favorite

import androidx.room.Entity
import androidx.room.PrimaryKey
import kurd.reco.core.api.model.HomeItemModel

@Entity(tableName = "favorite_table")
data class Favorite(
    @PrimaryKey val id: String,
    val title: String,
    val image: String,
    val isSeries: Boolean,
    val isLiveTv: Boolean,
    val pluginID: String
) {
    fun toHomeItemModel(): HomeItemModel {
        return HomeItemModel(
            id = id,
            title = title,
            poster = image,
            isSeries = isSeries,
            isLiveTv = isLiveTv
        )
    }
}
