package com.example.worktracker

import android.content.Context
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

class NotificationWorker(
    val context: Context,
    params: WorkerParameters
) : Worker(context, params) {
    override fun doWork(): Result {
        Log.d("MyNotificationWorker", "restarting work")
        NotificationHandler.updateNotification(context)
        val notificationRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(1, TimeUnit.MINUTES)
            .build()
        NotificationHandler.workRequestId = notificationRequest.id
        WorkManager.getInstance(context).enqueue(notificationRequest)
        return Result.success()
    }
}