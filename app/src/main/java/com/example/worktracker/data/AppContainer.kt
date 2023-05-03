package com.example.worktracker.data

import android.content.Context
import com.example.worktracker.Constants.PREFS_FILE_NAME

interface AppContainer {
    val shiftsRepository: ShiftsRepository
    val sharedPreferencesRepository: SharedPreferencesRepository
}

class AppDataContainer(private val context: Context) : AppContainer {
    override val shiftsRepository: ShiftsRepository by lazy {
        OfflineShiftsRepository(WorkTrackerDatabase.getDatabase(context).shiftDao())
    }

    override val sharedPreferencesRepository: SharedPreferencesRepository by lazy {
        val sharedPreferences = context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
        SharedPreferencesRepository(sharedPreferences)
    }
}