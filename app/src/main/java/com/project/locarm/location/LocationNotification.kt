package com.project.locarm.location

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.project.locarm.R
import com.project.locarm.ui.main.MainActivity

class LocationNotification(
    private val applicationContext: Context,
    private val foregroundNotificationId: Int
) {
    private lateinit var notificationManager: NotificationManager

    init {
        createNotificationChannels()
    }

    fun getForegroundNotification(
        foreGroundLayout: RemoteViews,
    ): Notification {
        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(applicationContext, CHANNEL_ID)


        builder
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(foreGroundLayout)
            .setContentIntent(notificationClickPendingIntent())
            .setDeleteIntent(notificationDeletePendingIntent())
            .setContentTitle(applicationContext.getString(R.string.backgroundLocationUpdate_location_info))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setSmallIcon(R.drawable.locarm_icon_png)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)

        return builder.build()
    }

    fun updateForegroundNotification(notification: Notification) {
        notificationManager.notify(foregroundNotificationId, notification)
    }

    fun startVibrationNotification(
        title: String,
        content: String,
    ) {
        val builder =
            NotificationCompat.Builder(applicationContext, CLOSE_TO_DESTINATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.locarm_icon_png)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()

        notificationManager.notify(foregroundNotificationId, builder)
    }

    private fun createNotificationChannels() {
        notificationManager =
            applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val foregroundChannel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            enableVibration(false)
        }

        val closeDestinationChannel = NotificationChannel(
            CLOSE_TO_DESTINATION_CHANNEL_ID,
            CLOSE_TO_DESTINATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 300, 200, 300)
        }

        notificationManager.createNotificationChannels(
            listOf(
                foregroundChannel,
                closeDestinationChannel
            )
        )
    }

    private fun notificationClickPendingIntent(): PendingIntent {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        return PendingIntent.getActivity(
            applicationContext,
            0 /* Request code */,
            intent,
            PendingIntent.FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT
        )
    }

    private fun notificationDeletePendingIntent(): PendingIntent {
        val deleteIntent =
            Intent(applicationContext, BackgroundLocationUpdateService::class.java).apply {
                action = DELETE
            }
        return PendingIntent.getService(
            applicationContext,
            0,
            deleteIntent,
            PendingIntent.FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT
        )
    }

    companion object {
        private const val CHANNEL_ID = "BackgroundLocationUpdateService"
        private const val CHANNEL_NAME = "BackgroundLocationUpdateService"

        private const val CLOSE_TO_DESTINATION_CHANNEL_ID = "CloseToDestination"
        private const val CLOSE_TO_DESTINATION_CHANNEL_NAME = "목적지 인접"

        private const val DELETE = "delete"
    }
}
