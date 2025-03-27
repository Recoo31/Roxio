package kurd.reco.core.data.di

import kurd.reco.core.SettingsDataStore
import kurd.reco.core.data.provideDatabase
import kurd.reco.core.data.provideDeletedPluginDao
import kurd.reco.core.data.provideFavoriteDao
import kurd.reco.core.data.provideAppDatabase
import kurd.reco.core.data.providePluginDao
import kurd.reco.core.data.provideWatchedItemDao
import kurd.reco.core.plugin.PluginManager
import kurd.reco.core.viewmodels.DiscoverVM
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataBaseModule = module {
    single { provideDatabase(get()) }
    single { providePluginDao(get()) }
    single { provideDeletedPluginDao(get()) }

    single { provideAppDatabase(get()) }
    single { provideFavoriteDao(get()) }
    single { provideWatchedItemDao(get()) }

    single { SettingsDataStore(androidContext()) }
}

val viewModelModule = module {
    single { PluginManager(get(), get(), androidContext()) }
    factory { DiscoverVM(get()) }
}