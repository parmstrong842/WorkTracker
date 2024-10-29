package com.example.worktracker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.example.worktracker.Constants.START_OF_WEEK_KEY
import com.example.worktracker.Constants.TIME_ZONE_KEY
import com.example.worktracker.data.AppContainer
import com.example.worktracker.data.AppDataContainer
import java.time.DayOfWeek
import java.util.*

//TODO if shift is longer then 24h it messes up
//TODO find out why app is accessing network activity
//TODO import csv file
//TODO make notification not dismissible
//TODO add guard rails for clocking out and ending break
//TODO update dark theme animation

const val TAG = "WorkTrackerTag"
class WorkTrackerApplication : Application() {
    /**
     * AppContainer instance used by the rest of classes to obtain dependencies
     */
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
        val sharedPrefs = container.sharedPreferencesRepository
        createNotificationChannel(this)

        NotificationHandler.initialize(sharedPrefs)
        Utils.initialize(sharedPrefs)

        if (!sharedPrefs.contains(TIME_ZONE_KEY)) {
            val userTimeZone = TimeZone.getDefault().id
            sharedPrefs.putString(TIME_ZONE_KEY, userTimeZone)
        }

        if (!sharedPrefs.contains(START_OF_WEEK_KEY)) {
            val firstDayOfWeek = getStartOfWeekString()
            sharedPrefs.putString(START_OF_WEEK_KEY, firstDayOfWeek)
        }
    }

    private fun getStartOfWeekString(): String {
        val calendar = Calendar.getInstance()
        val dayOfWeek = when (calendar.firstDayOfWeek) {
            Calendar.SUNDAY -> DayOfWeek.SUNDAY
            Calendar.MONDAY -> DayOfWeek.MONDAY
            Calendar.TUESDAY -> DayOfWeek.TUESDAY
            Calendar.WEDNESDAY -> DayOfWeek.WEDNESDAY
            Calendar.THURSDAY -> DayOfWeek.THURSDAY
            Calendar.FRIDAY -> DayOfWeek.FRIDAY
            Calendar.SATURDAY -> DayOfWeek.SATURDAY
            else -> throw IllegalArgumentException(getString(R.string.invalid_day_of_the_week))
        }
        return dayOfWeek.name
    }

    private fun createNotificationChannel(context: Context) {
        val channelId = getString(R.string.channel_id)
        val name = getString(R.string.worktracker_notification_name)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_LOW).apply {
            setShowBadge(false)
        }
        notificationManager.createNotificationChannel(channel)
    }
}