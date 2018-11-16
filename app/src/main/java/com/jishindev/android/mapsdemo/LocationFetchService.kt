package com.jishindev.android.mapsdemo

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import timber.log.Timber

class LocationFetchService : Service() {

    // todo inject this
    private lateinit var notificationManager: NotificationManager
    private val binder = LocalBinder()
    private var isChangingConfiguration: Boolean = false
    private val notification: Notification
        get() {

            val title = "Return to MapsDemo"
            val activityPendingIntent = PendingIntent.getActivity(
                this, 0,
                Intent(this, MainActivity::class.java), 0
            )

            return NotificationCompat.Builder(this, "Ongoing")
                .setContentIntent(activityPendingIntent)
                .setContentTitle(title)
                .setOngoing(true)
                .setPriority(
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N)
                        NotificationManager.IMPORTANCE_HIGH
                    else
                        Notification.PRIORITY_HIGH
                )
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
                .setTicker(title)
                .setWhen(System.currentTimeMillis()).build()
        }

    override fun onCreate() {
        super.onCreate()

        // todo inject this
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.i("onStartCommand() called with: intent = [$intent], flags = [$flags], startId = [$startId]")
        return Service.START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        Timber.i("onBind() called with: intent = [$intent]")
        stopForeground(true)
        isChangingConfiguration = false
        return binder
    }

    override fun onRebind(intent: Intent) {
        Timber.i("onRebind() called with: intent = [$intent]")
        stopForeground(true)
        isChangingConfiguration = false
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent): Boolean {
        Timber.i("onUnbind() called with: intent = [$intent]")

        if (!isChangingConfiguration) {
            Timber.i("onUnbind: starting foreground fetchService")

            startForeground(NOTIFICATION_ID, notification)
        }
        return true
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        Timber.i("onConfigurationChanged() called with: newConfig = [$newConfig]")
        super.onConfigurationChanged(newConfig)
        isChangingConfiguration = true
    }


    companion object {
        const val NOTIFICATION_ID = 1001
    }

    inner class LocalBinder : Binder() {
        val fetchService: LocationFetchService
            get() = this@LocationFetchService
    }
}