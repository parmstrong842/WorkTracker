package com.example.worktracker

import android.app.Application
import android.content.Context
import com.example.worktracker.Constants.PREFS_FILE_NAME
import com.example.worktracker.Constants.START_OF_WEEK_KEY
import com.example.worktracker.Constants.TIME_ZONE_KEY
import com.example.worktracker.data.AppContainer
import com.example.worktracker.data.AppDataContainer
import java.text.DateFormatSymbols
import java.util.*

//TODO use System.currentTimeMillis()
const val TAG = "WorkTrackerTag"
class WorkTrackerApplication : Application() {
    /**
     * AppContainer instance used by the rest of classes to obtain dependencies
     */
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)

        val sharedPreferences = getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)

        if (!sharedPreferences.contains(TIME_ZONE_KEY)) {
            val userTimeZone = TimeZone.getDefault().id
            sharedPreferences.edit().putString(TIME_ZONE_KEY, userTimeZone).apply()
        }

        if (!sharedPreferences.contains(START_OF_WEEK_KEY)) {
            val firstDayOfWeek = getStartOfWeekString()
            sharedPreferences.edit().putString(START_OF_WEEK_KEY, firstDayOfWeek).apply()
        }
    }

    private fun getStartOfWeekString(): String {
        val calendar = Calendar.getInstance()
        val firstDayOfWeek = calendar.firstDayOfWeek
        val dateFormatSymbols = DateFormatSymbols.getInstance()
        return dateFormatSymbols.weekdays[firstDayOfWeek].uppercase()
    }
}