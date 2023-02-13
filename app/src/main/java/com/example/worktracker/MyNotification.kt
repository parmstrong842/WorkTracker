package com.example.worktracker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

class MyNotification {
    private val channelId = "ID"
    private val notificationId = 100


    fun fireNotification(context: Context, title: String, msg: String) {
        val name = "WorkTrackerNotification"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(channel)

        val mainIntent = Intent(context, MainActivity::class.java)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, mainIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification: Notification = Notification.Builder(context, Constants.CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(msg)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    fun cancelNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)
    }
}