package kurd.reco.mobile

import android.app.Application
import kurd.reco.core.checkPackageInfoCreatorIntegrity
import kurd.reco.core.data.di.dataBaseModule
import kurd.reco.mobile.data.di.exoplayerModule
import kurd.reco.mobile.data.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class KtorApp : Application() {
    override fun onCreate() {
        super.onCreate()
        checkPackageInfoCreatorIntegrity()
        startKoin {
            androidContext(this@KtorApp)
            modules(viewModelModule, dataBaseModule, exoplayerModule)
        }
    }
}