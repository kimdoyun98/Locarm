package com.project.locarm.location

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_ONE_SHOT
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.Vibrator
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.project.locarm.R
import com.project.locarm.common.MyApplication
import com.project.locarm.common.PreferenceUtil.Companion.DISTANCE
import com.project.locarm.common.PushAlarm
import com.project.locarm.main.MainActivity
import java.text.DecimalFormat


class BackgroundLocationUpdateService : Service() {
    private lateinit var context: Context
    private lateinit var realTimeLocation: RealTimeLocation
    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        context = this
        realTimeLocation = RealTimeLocation(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground()

        realTimeLocation.getLocation(
            { a, b -> updateNotification(a, b) },
            { distance -> vibrateWithAlarm(distance) }
        )

        return START_STICKY
    }

    private fun vibrateWithAlarm(distance: Int) {
        if (distance > MyApplication.prefs.getAlarmDistance(DISTANCE)) return
        Log.e("현재 위치", "목적지 인접")
        PushAlarm.build(
            context,
            "목적지 인접",
            "목적지까지 ${DecimalFormat("##0.0").format(distance.toDouble() / 1000)}KM 남았습니다.",
            MyApplication.prefs.getAddress("name", "")!!
        )
        val vibrator: Vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(1000)
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun startForeground() {
        createNotificationChannel()
        startForeground(101, getNotification())
    }

    private fun createNotificationChannel() {
        notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        notificationManager.createNotificationChannel(channel)
    }

    private fun updateNotification(a: Double, b: Double) {
        notificationManager.notify(101, getNotification(a, b))
    }

    private fun getNotification(a: Double = 0.0, b: Double = 0.0): Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0 /* Request code */,
            intent,
            PendingIntent.FLAG_IMMUTABLE or FLAG_ONE_SHOT
        )

        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(applicationContext, CHANNEL_ID)

        val notificationLayout =
            RemoteViews(packageName, R.layout.location_foreground_layout).apply {
                setTextViewText(R.id.latitude, "$a")
                setTextViewText(R.id.longitude, "$b")
            }

        builder
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(notificationLayout)
            .setContentIntent(pendingIntent)
            .setContentTitle("위치 정보")
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_LOCATION_SHARING)
            .setSmallIcon(R.drawable.ic_launcher_background)

        return builder.build()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        Log.e(TAG, "Service Stopped")
        realTimeLocation.onDestroy()
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL_ID = "channel_location"
        private const val CHANNEL_NAME = "channel_location"
        private const val TAG = "BackgroundLocationUpdateService"
    }
}
