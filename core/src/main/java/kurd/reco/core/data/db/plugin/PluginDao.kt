package kurd.reco.core.data.db.plugin

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface PluginDao {
    @Query("SELECT * FROM plugins")
    fun getAllPlugins(): List<Plugin>

    @Query("SELECT * FROM plugins")
    fun getAllPluginsFlow(): Flow<List<Plugin>>

    @Query("SELECT * FROM plugins WHERE id = :pluginId")
    fun getPluginById(pluginId: String): Plugin?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPlugin(plugin: Plugin)

    @Query("DELETE FROM plugins WHERE id = :pluginId")
    fun deletePlugin(pluginId: String)

    @Query("UPDATE plugins SET isSelected = 0 WHERE isSelected = 1")
    fun clearSelectedPlugin()

    @Query("UPDATE plugins SET isSelected = 1 WHERE id = :pluginId")
    fun selectPlugin(pluginId: String)

    @Query("SELECT * FROM plugins WHERE isSelected = 1 LIMIT 1")
    fun getSelectedPlugin(): Plugin?

    @Query("UPDATE plugins SET active = :active WHERE id = :pluginId")
    fun updatePluginActiveStatus(pluginId: String, active: Boolean)
}

@Dao
interface DeletedPluginDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDeletedPlugin(deletedPlugin: DeletedPlugin)

    @Query("SELECT * FROM deleted_plugins WHERE id = :pluginId")
    fun getDeletedPluginById(pluginId: String): DeletedPlugin?

    @Query("SELECT * FROM deleted_plugins")
    fun getAllDeletedPlugins(): List<DeletedPlugin>

    @Query("DELETE FROM deleted_plugins WHERE id = :pluginId")
    fun deleteDeletedPlugin(pluginId: String)
}
