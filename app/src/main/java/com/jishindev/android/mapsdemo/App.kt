package com.jishindev.android.mapsdemo

import android.app.Application
import android.content.SharedPreferences
import androidx.work.WorkManager
import com.google.android.gms.location.LocationServices
import com.jishindev.android.mapsdemo.network.ServerInterface
import com.jishindev.android.mapsdemo.utils.PreferenceHelper
import org.koin.android.ext.android.startKoin
import org.koin.androidx.viewmodel.experimental.builder.viewModel
import org.koin.dsl.module.module
import timber.log.Timber

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())

        startKoin(this, listOf(module {
            single { LocationServices.getFusedLocationProviderClient(this@App) }
            single { PreferenceHelper.prefs(this@App) }
            single { WorkManager.getInstance() }
            single { ServerInterface.getApi() }
            viewModel<MainVM>()
        }))
    }
}