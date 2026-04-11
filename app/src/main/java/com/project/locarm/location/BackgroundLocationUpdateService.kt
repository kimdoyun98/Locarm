package com.project.locarm.location

import android.Manifest
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
import android.widget.RemoteViews
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import com.project.locarm.R
import com.project.locarm.common.PreferenceUtil
import com.project.locarm.common.PreferenceUtil.Companion.DISTANCE
import com.project.locarm.common.appContainer
import com.project.locarm.common.permission.LocarmPermission
import com.project.locarm.data.model.Loc
import com.project.locarm.data.model.SelectDestination
import com.project.locarm.data.repository.LocationRepository
import com.project.locarm.location.util.GeoCoder
import com.project.locarm.ui.main.MainActivity
import com.project.locarm.ui.main.MainActivity.Companion.DISTANCE_REMAINING
import com.project.locarm.ui.main.MainActivity.Companion.SELECT

class BackgroundLocationUpdateService : Service() {
    private lateinit var pref: PreferenceUtil
    private val mBinder: IBinder = LocationServiceBind()
    private lateinit var context: Context
    private lateinit var realTimeLocation: RealTimeLocation
    private lateinit var notificationManager: NotificationManager
    private var startLocation: Loc? = null
    private var destination: SelectDestination? = null
    private val alarmDistance by lazy { pref.getAlarmDistance(DISTANCE) }
    private lateinit var locationRepository: LocationRepository

    inner class LocationServiceBind : Binder() {
        fun getService() = this@BackgroundLocationUpdateService

        fun getDestination(): SelectDestination? = destination
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        realTimeLocation = applicationContext.appContainer.realTimeLocation
        pref = applicationContext.appContainer.preference
        locationRepository = applicationContext.appContainer.locationRepository

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

                locationRepository.updateDistance(
                    intent?.getIntExtra(DISTANCE_REMAINING, 0)!!
                )

                startForeground()

                realTimeLocation.getLocation(
                    destination!!,
                    { distance ->
                        locationRepository.updateDistance(distance)
                        updateNotification(distance)
                    },
                    { distance ->
                        if ((LocarmPermission.checkTiramisuVersionHigher() && LocarmPermission.checkNotificationPermission(
                                this
                            )) || !LocarmPermission.checkTiramisuVersionHigher()
                        ) {
                            vibrateWithAlarm(distance)
                        }
                    }
                )
            }
        }

        return START_REDELIVER_INTENT
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun vibrateWithAlarm(distance: Int) {
        if (distance > alarmDistance) return

        val builder = NotificationCompat.Builder(this, CLOSE_TO_DESTINATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.locarm_icon_png)
            .setContentTitle(getString(R.string.backgroundLocationUpdate_destination_nearby))
            .setContentText("${destination!!.name} 남은 거리: ${GeoCoder.getDistanceKmToString(distance)}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(FOREGROUND_NOTIFICATION_ID, builder)
    }

    private fun startForeground() {
        createNotificationChannel()
        startForeground(FOREGROUND_NOTIFICATION_ID, getNotification())
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

        val closeDestinationChannel = NotificationChannel(
            CLOSE_TO_DESTINATION_CHANNEL_ID,
            CLOSE_TO_DESTINATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 300, 200, 300)
        }

        notificationManager.createNotificationChannels(listOf(channel, closeDestinationChannel))
    }

    private fun updateNotification(distance: Int) {
        notificationManager.notify(FOREGROUND_NOTIFICATION_ID, getNotification(distance))
    }

    private fun getNotification(
        distance: Int = pref.getAlarmDistance(DISTANCE)
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
                    GeoCoder.getDistanceKmToString(distance)
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
            .setSmallIcon(R.drawable.locarm_icon_png)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)

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

        private const val CLOSE_TO_DESTINATION_CHANNEL_ID = "CloseToDestination"
        private const val CLOSE_TO_DESTINATION_CHANNEL_NAME = "목적지 인접"

        private const val FOREGROUND_NOTIFICATION_ID = 101
        private const val CLOSE_DESTINATION_NOTIFICATION_ID = 102
        private const val DELETE = "delete"
    }
}
