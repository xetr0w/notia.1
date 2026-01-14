package com.notia.app

import android.app.Application
import timber.log.Timber

class NotiaApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        
        Timber.d("NotiaApp initialized")
    }
}
