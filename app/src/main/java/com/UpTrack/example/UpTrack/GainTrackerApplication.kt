package com.UpTrack.example.UpTrack

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen

class GainTrackerApplication : Application() {

    lateinit var appContainer: AppContainer

    override fun onCreate() {
        super.onCreate()

        appContainer = AppContainer(this)
        AndroidThreeTen.init(this)
    }
}
