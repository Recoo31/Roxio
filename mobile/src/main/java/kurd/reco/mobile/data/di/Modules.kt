package kurd.reco.mobile.data.di

import kurd.reco.core.viewmodels.AuthVM
import kurd.reco.core.viewmodels.MainVM
import kurd.reco.core.plugin.PluginManager
import kurd.reco.core.viewmodels.DetailVM
import kurd.reco.core.viewmodels.HomeVM
import kurd.reco.mobile.ui.search.SearchVM
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    single { MainVM(get(), get()) }
    single { PluginManager(get(), get(), androidContext()) }
    single { HomeVM(get()) }
    viewModel { DetailVM(get()) }
    single { SearchVM(get()) }
    viewModel { AuthVM() }
}

//val exoplayerModule = module {
//    single { exoPlayerDatabaseProvider(androidContext()) }
//}
//
//
//@OptIn(UnstableApi::class)
//fun exoPlayerDatabaseProvider(context: Context) = StandaloneDatabaseProvider(context)