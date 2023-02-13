package com.example.worktracker

import android.app.Application
import com.example.worktracker.data.AppContainer
import com.example.worktracker.data.AppDataContainer

class WorkTrackerApplication : Application() {
    /**
     * AppContainer instance used by the rest of classes to obtain dependencies
     */
    lateinit var container: AppContainer
    val test: String = "Hello World!"
    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}