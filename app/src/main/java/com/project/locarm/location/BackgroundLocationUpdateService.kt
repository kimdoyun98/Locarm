package com.project.locarm.location

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Vibrator
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.project.locarm.R
import com.project.locarm.common.MyApplication
import com.project.locarm.common.PreferenceUtil.Companion.DISTANCE
import com.project.locarm.common.PushAlarm
import com.project.locarm.data.Loc
import com.project.locarm.data.SelectDestination
import com.project.locarm.main.MainActivity
import com.project.locarm.main.MainActivity.Companion.SELECT
import java.text.DecimalFormat

class BackgroundLocationUpdateService : Service() {
    private val mBinder: IBinder = LocationServiceBind()
    private lateinit var context: Context
    private lateinit var realTimeLocation: RealTimeLocation
    private lateinit var notificationManager: NotificationManager
    private var startLocation: Loc? = null
    private var destination: SelectDestination? = null
    private val alarmDistance = MyApplication.prefs.getAlarmDistance(DISTANCE)

    inner class LocationServiceBind : Binder() {
        fun getService() = this@BackgroundLocationUpdateService

        fun getDestination(): SelectDestination? = destination
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        realTimeLocation = RealTimeLocation(this)

        realTimeLocation.currentLocation()?.addOnSuccessListener {
            startLocation = Loc(
                latitude = it.latitude,
                longitude = it.longitude
            )
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            DELETE -> {
                stopService(intent)
            }

            else -> {
                destination = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent?.getParcelableExtra(SELECT, SelectDestination::class.java)
                } else {
                    intent?.getParcelableExtra(SELECT)
                }

                startForeground()

                realTimeLocation.getLocation(
                    destination!!,
                    { distance -> updateNotification(distance) },
                    { distance -> vibrateWithAlarm(distance) }
                )
            }
        }

        return START_STICKY
    }

    private fun vibrateWithAlarm(distance: Int) {
        if (distance > alarmDistance) return
        PushAlarm.build(
            context,
            getString(R.string.backgroundLocationUpdate_destination_nearby),
            getString(
                R.string.backgroundLocationUpdate_pushAlarm_content,
                DecimalFormat(KM_FORMAT_PATTERN).format(distance.toKm())
            ),
            destination!!.name
        )
        val vibrator: Vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(1000)
    }

    private fun Int.toKm(): Double = this.toDouble() / 1000

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
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            enableVibration(false)
        }

        notificationManager.createNotificationChannel(channel)
    }

    private fun updateNotification(distance: Int) {
        notificationManager.notify(101, getNotification(distance))
    }

    private fun getNotification(
        distance: Int = MyApplication.prefs.getAlarmDistance(DISTANCE)
    ): Notification {
        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(applicationContext, CHANNEL_ID)

        val totalDistance = if (startLocation != null) {
            realTimeLocation.getDistance(
                lat1 = startLocation!!.latitude,
                lon1 = startLocation!!.longitude,
                destination = destination!!
            )
        } else {
            1000
        }

        val foreGroundLayout =
            RemoteViews(packageName, R.layout.location_foreground_layout).apply {
                setTextViewText(
                    R.id.distance_tv,
                    DecimalFormat(KM_FORMAT_PATTERN).format(distance.toKm())
                )
                setProgressBar(
                    R.id.distance_progress_bar,
                    totalDistance,
                    totalDistance - distance,
                    false
                )

                setTextViewText(
                    R.id.destination_guide_tv,
                    getString(
                        R.string.backgroundLocationUpdate_remaining_distance,
                        destination?.name
                    )
                )
            }

        builder
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(foreGroundLayout)
            .setContentIntent(notificationClickPendingIntent())
            .setDeleteIntent(notificationDeletePendingIntent())
            .setContentTitle(getString(R.string.backgroundLocationUpdate_location_info))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_launcher_background)

        return builder.build()
    }

    private fun notificationClickPendingIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        return PendingIntent.getActivity(
            this,
            0 /* Request code */,
            intent,
            PendingIntent.FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT
        )
    }

    private fun notificationDeletePendingIntent(): PendingIntent {
        val deleteIntent = Intent(context, BackgroundLocationUpdateService::class.java).apply {
            action = DELETE
        }
        return PendingIntent.getService(
            this,
            0,
            deleteIntent,
            PendingIntent.FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT
        )
    }

    override fun onBind(p0: Intent?): IBinder {
        return mBinder
    }

    override fun onDestroy() {
        realTimeLocation.onDestroy()
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL_ID = "BackgroundLocationUpdateService"
        private const val CHANNEL_NAME = "BackgroundLocationUpdateService"
        private const val TAG = "BackgroundLocationUpdateService"

        private const val DELETE = "delete"
        private const val KM_FORMAT_PATTERN = "##0.0"
    }
}
