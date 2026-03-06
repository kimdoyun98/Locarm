package com.project.locarm.common

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.project.locarm.R

object PushAlarm {
    private const val CHANNEL_ID = "CHANNEL_ID"

    fun build(context: Context, title: String, content: String, summary: String) {
        val style = NotificationCompat.BigTextStyle()
            .setBigContentTitle(title)
            .setSummaryText(summary)
            .bigText(content)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(style)
            .setAutoCancel(false)
            .setShowWhen(true)
            .setColor(ContextCompat.getColor(context, R.color.purple_200))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        if (ActivityCompat.checkSelfPermission(
                MyApplication.instance,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED ||
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
        ) {
            NotificationManagerCompat.from(context).notify(1, builder.build())
        }
    }
}
