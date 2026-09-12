package com.sap.codelab.repository

import android.app.Application
import com.sap.codelab.AppDependencies

/**
 * Extension of the Android Application class.
 */
internal class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Repository.initialize(this)
        AppDependencies.initialize(this)
    }
}