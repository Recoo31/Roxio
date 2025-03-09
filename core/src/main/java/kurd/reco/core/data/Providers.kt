package kurd.reco.core.data

import android.app.Application
import androidx.room.Room
import kurd.reco.core.data.db.AppDatabase
import kurd.reco.core.data.db.favorite.FavoriteDao
import kurd.reco.core.data.db.plugin.DeletedPluginDao
import kurd.reco.core.data.db.plugin.MIGRATION_1_2
import kurd.reco.core.data.db.plugin.PluginDao
import kurd.reco.core.data.db.plugin.PluginDatabase
import kurd.reco.core.data.db.watched.WatchedItemDao


fun provideDatabase(application: Application): PluginDatabase =
    Room.databaseBuilder(
        application,
        PluginDatabase::class.java,
        "plugin_database"
    ).allowMainThreadQueries().addMigrations(MIGRATION_1_2).build()

fun providePluginDao(pluginDataBase: PluginDatabase): PluginDao = pluginDataBase.pluginDao()

fun provideDeletedPluginDao(pluginDataBase: PluginDatabase): DeletedPluginDao = pluginDataBase.deletedPluginDao()
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

fun provideAppDatabase(application: Application): AppDatabase =
    Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        "app_database"
    ).fallbackToDestructiveMigration().allowMainThreadQueries().build()

fun provideFavoriteDao(appDatabase: AppDatabase): FavoriteDao = appDatabase.favoriteDao()
fun provideWatchedItemDao(appDatabase: AppDatabase): WatchedItemDao = appDatabase.watchedItemDao()