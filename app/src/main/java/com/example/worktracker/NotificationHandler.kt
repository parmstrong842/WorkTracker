package com.example.worktracker

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.worktracker.Utils.getBreakCounter
import com.example.worktracker.Utils.getCounter
import com.example.worktracker.Utils.getDisplayTimeAtTimeZone
import com.example.worktracker.Utils.reformatTime
import com.example.worktracker.data.SharedPreferencesRepository
import java.time.ZoneId
import java.util.Calendar
import java.util.UUID
import java.util.concurrent.TimeUnit


object NotificationHandler {

    private lateinit var sharedPref: SharedPreferencesRepository
    lateinit var workRequestId: UUID

    fun initialize(sp: SharedPreferencesRepository) {
        sharedPref = sp
        val savedWorkRequestId = sharedPref.getString(Constants.WORK_REQUEST_ID_KEY, "")
        if (savedWorkRequestId.isNotEmpty()) {
            workRequestId = UUID.fromString(savedWorkRequestId)
        }
    }

    fun startRecurringNotification(context: Context) {
        fireNotification(context)
        val calendar = Calendar.getInstance().apply {
            add(Calendar.MINUTE, 1)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val delay = calendar.timeInMillis - System.currentTimeMillis()
        val notificationRequest = OneTimeWorkRequestBuilder<NotificationWorker>()//TODO not always in sync
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()
        workRequestId = notificationRequest.id
        sharedPref.putString(Constants.WORK_REQUEST_ID_KEY, workRequestId.toString())
        WorkManager.getInstance(context)
            .enqueue(notificationRequest)
        Log.d("MyNotificationWorker", "starting work")
    }

    fun endRecurringNotification(context: Context) {
        cancelNotification(context)
        WorkManager.getInstance(context).cancelWorkById(workRequestId)
        sharedPref.remove(Constants.WORK_REQUEST_ID_KEY)
    }

    fun fireNotification(context: Context) {
        if (sharedPref.getBoolean(Constants.CLOCKED_IN_KEY, false)) {
            val breakTotal = getBreakTotal()
            val shiftTotal = "Shift Total: ${getCounter()} $breakTotal"
            val shiftStart = "Shift Start: ${getStartTime(Constants.SHIFT_START_KEY)}"
            sendNotification(context, shiftTotal, shiftStart)
        } else {
            Log.d("NotificationHandler", "fired notification but not clocked it")
        }
    }

    private fun getStartTime(key: String): String {
        val timeZoneString = sharedPref.getString(Constants.TIME_ZONE_KEY, "")
        val timeZone = ZoneId.of(timeZoneString)
        val timestamp = sharedPref.getString(key, "")
        return getDisplayTimeAtTimeZone(timeZone, timestamp)
    }

    private fun getBreakTotal(): String {
        val breakTotal = sharedPref.getString(Constants.BREAK_TOTAL_KEY, "")
        return if (breakTotal != "")
            "(Break: ${reformatTime(breakTotal)})"
        else breakTotal
    }

    fun fireBreakNotification(context: Context) {
        if (sharedPref.getBoolean(Constants.ON_BREAK_KEY, false)) {
            val breakTotal = "Break Total: ${getBreakCounter()}"
            val breakStart = "Break Start: ${getStartTime(Constants.BREAK_START_KEY)}"
            sendNotification(context, breakTotal, breakStart)
        }
    }

    fun updateNotification(context: Context) {
        if (sharedPref.getBoolean(Constants.ON_BREAK_KEY, false)) {
            fireBreakNotification(context)
        } else {
            fireNotification(context)
        }
    }

    private fun sendNotification(context: Context, total: String, start: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val mainIntent = Intent(context, MainActivity::class.java)
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(context, 0, mainIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification: Notification = Notification.Builder(context, context.getString(R.string.channel_id))
            .setContentTitle(total)
            .setContentText(start)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .build()

        notificationManager.notify(Constants.NOTIFICATION_ID, notification)
    }

    private fun cancelNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(Constants.NOTIFICATION_ID)
    }
}