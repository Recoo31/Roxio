package kurd.reco.roxio

import android.app.Application
import kurd.reco.core.data.di.dataBaseModule
import kurd.reco.roxio.data.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class KtorApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@KtorApp)
            modules(viewModelModule, dataBaseModule)
        }
    }
}