package com.example.worktracker.data

import android.content.Context

interface AppContainer {
    val shiftsRepository: ShiftsRepository
}

class AppDataContainer(private val context: Context) : AppContainer {
    override val shiftsRepository: ShiftsRepository by lazy {
        OfflineShiftsRepository(WorkTrackerDatabase.getDatabase(context).shiftDao())
    }
}