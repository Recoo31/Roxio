package kurd.reco.core.data.db.favorite

import androidx.room.Database
import androidx.room.RoomDatabase
import kurd.reco.core.data.db.favorite.Favorite
import kurd.reco.core.data.db.favorite.FavoriteDao

@Database(entities = [Favorite::class], version = 1, exportSchema = false)
abstract class FavoriteDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
}