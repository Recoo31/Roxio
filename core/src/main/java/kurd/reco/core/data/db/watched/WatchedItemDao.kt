package kurd.reco.core.data.db.watched

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchedItemDao {
    @Query("SELECT * FROM watched_items")
    fun getAllWatchedItems(): Flow<List<WatchedItemModel>>

    @Query("SELECT * FROM watched_items WHERE id = :itemId")
    fun getWatchedItemById(itemId: String): WatchedItemModel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdateWatchedItem(watchedItem: WatchedItemModel)

    @Query("DELETE FROM watched_items WHERE id = :itemId")
    fun deleteWatchedItemById(itemId: String)

    @Query("DELETE FROM watched_items")
    fun deleteAllWatchedItems()
}
