package kurd.reco.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kurd.reco.core.data.db.favorite.Favorite
import kurd.reco.core.data.db.favorite.FavoriteDao
import kurd.reco.core.data.db.watched.Converters
import kurd.reco.core.data.db.watched.WatchedItemDao
import kurd.reco.core.data.db.watched.WatchedItemModel

@Database(entities = [WatchedItemModel::class, Favorite::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun watchedItemDao(): WatchedItemDao
    abstract fun favoriteDao(): FavoriteDao
}
