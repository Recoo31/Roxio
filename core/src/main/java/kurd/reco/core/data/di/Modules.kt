package kurd.reco.core.data.di

import kurd.reco.core.SettingsDataStore
import kurd.reco.core.data.provideDatabase
import kurd.reco.core.data.provideDeletedPluginDao
import kurd.reco.core.data.provideFavoriteDao
import kurd.reco.core.data.provideFavoriteDatabase
import kurd.reco.core.data.providePluginDao
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataBaseModule = module {
    single { provideDatabase(get()) }
    single { providePluginDao(get()) }
    single { provideDeletedPluginDao(get()) }

    single { provideFavoriteDatabase(get()) }
    single { provideFavoriteDao(get()) }

    single { SettingsDataStore(androidContext()) }
}