package com.example.worktracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("MyBootReceiver", "BOOT")
        if (Intent.ACTION_BOOT_COMPLETED == intent?.action) {
            val sharedPref = context.getSharedPreferences(Constants.PREFS_FILE_NAME, Context.MODE_PRIVATE)
            val clockedIn = sharedPref.getBoolean(Constants.CLOCKED_IN_KEY, false)
            val onBreak = sharedPref.getBoolean(Constants.ON_BREAK_KEY, false)

            if (clockedIn) {
                if(onBreak) {
                    NotificationHandler.fireBreakNotification(context)
                } else {
                    NotificationHandler.fireNotification(context)
                }
            }
        }
    }
}