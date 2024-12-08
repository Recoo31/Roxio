package kurd.reco.roxio.data.di

import kurd.reco.core.AuthVM
import kurd.reco.core.MainVM
import kurd.reco.core.plugin.PluginManager
import kurd.reco.roxio.ui.detail.DetailVM
import kurd.reco.roxio.ui.home.HomeVM
import kurd.reco.roxio.ui.search.SearchVM
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    single { MainVM(get(), get()) }
    single { PluginManager(get(), get(), androidContext(), get()) }
    single { HomeVM(get()) }
    viewModel { DetailVM(get()) }
    single { SearchVM(get()) }
    viewModel { AuthVM() }
}