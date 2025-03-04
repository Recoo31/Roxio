package kurd.reco.mobile.data.di

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import kurd.reco.core.AuthVM
import kurd.reco.core.MainVM
import kurd.reco.core.plugin.PluginManager
import kurd.reco.mobile.ui.detail.DetailVM
import kurd.reco.mobile.ui.downloader.MediaDownloadHelper
import kurd.reco.mobile.ui.home.HomeVM
import kurd.reco.mobile.ui.search.SearchVM
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

val exoplayerModule = module {
    single { exoPlayerDatabaseProvider(androidContext()) }
    single { MediaDownloadHelper(androidContext(), get()) }

}


@OptIn(UnstableApi::class)
fun exoPlayerDatabaseProvider(context: Context) = StandaloneDatabaseProvider(context)