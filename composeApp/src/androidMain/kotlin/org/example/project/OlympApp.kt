package org.example.project

import android.app.Application
import org.example.project.di.dataStoreModule
import org.example.project.di.gameModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class OlympApp : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@OlympApp)
            modules(
                dataStoreModule,
                gameModule
            )
        }
    }
}