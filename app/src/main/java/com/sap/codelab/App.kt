package com.sap.codelab

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager

/**
 * Extension of the Android Application class.
 */
internal class App : Application() {
    override fun onCreate() {
        super.onCreate()
        AppDependencies.initialize(this)
        WorkManager.initialize(
            this,
            Configuration.Builder()
                .setWorkerFactory(AppDependencies.workerFactory)
                .build()
        )
    }
}
