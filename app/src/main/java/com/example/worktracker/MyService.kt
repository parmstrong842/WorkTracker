package com.example.worktracker

import android.app.*
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.worktracker.Constants.START_FOREGROUND
import com.example.worktracker.Constants.STOP_FOREGROUND

class MyService : Service() {

    private val tag = "MyService"

    init {
        Log.d(tag, "Service is created...")
        isRunning = true
    }

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val data = intent?.getStringExtra("EXTRA_DATA")
        data?.let {
            //Log.d(TAG, "EXTRA_DATA: $data")
        }

        if (intent?.action == START_FOREGROUND) {
            Log.d(tag, "Received Start Foreground Intent")

            val mainIntent = Intent(this, MainActivity::class.java)
            val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, mainIntent, PendingIntent.FLAG_IMMUTABLE)


            val notification: Notification = Notification.Builder(this, Constants.CHANNEL_ID)
                .setContentTitle("Clocked In")
                .setContentText("You are clocked in")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true)
                .build()

            startForeground(1, notification)
        } else if (intent?.action == STOP_FOREGROUND) {
            Log.d(tag, "Received Stop Foreground Intent")
            stopForeground(STOP_FOREGROUND_DETACH)
            stopSelfResult(startId)
        }

        return START_STICKY
    }

    override fun onDestroy() {
        Log.d(tag, "Service is destroyed...")
        isRunning = false
    }

    companion object {
        var isRunning = false
    }
}