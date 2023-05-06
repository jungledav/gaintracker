package com.example.gaintracker

import android.app.Application

class GainTrackerApplication : Application() {

    lateinit var appContainer: AppContainer

    override fun onCreate() {
        super.onCreate()

        appContainer = AppContainer(this)
    }
}
