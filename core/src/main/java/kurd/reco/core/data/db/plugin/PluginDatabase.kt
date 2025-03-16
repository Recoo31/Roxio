package kurd.reco.core.data.db.plugin

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE plugins_temp (
                id TEXT PRIMARY KEY NOT NULL,
                name TEXT NOT NULL,
                classPath TEXT NOT NULL,
                className TEXT NOT NULL,
                filePath TEXT NOT NULL,
                version TEXT NOT NULL,
                downloadUrl TEXT NOT NULL DEFAULT '',
                image TEXT,
                active INTEGER NOT NULL,
                isSelected INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())

        database.execSQL("""
            INSERT INTO plugins_temp (id, name, classPath, className, filePath, version, downloadUrl, image, active, isSelected)
            SELECT id, name, classPath, className, filePath, version, downloadUrl, image, active, isSelected FROM plugins
        """.trimIndent())

        database.execSQL("DROP TABLE plugins")

        database.execSQL("ALTER TABLE plugins_temp RENAME TO plugins")
    }
}




@Database(entities = [Plugin::class, DeletedPlugin::class], version = 3, exportSchema = false)
abstract class PluginDatabase : RoomDatabase() {
    abstract fun pluginDao(): PluginDao
    abstract fun deletedPluginDao(): DeletedPluginDao
}
