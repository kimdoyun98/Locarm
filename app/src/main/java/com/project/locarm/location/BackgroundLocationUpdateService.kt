package com.project.locarm.location

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Vibrator
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.project.locarm.R
import com.project.locarm.common.MyApplication
import com.project.locarm.common.PreferenceUtil.Companion.DISTANCE
import com.project.locarm.common.PushAlarm
import com.project.locarm.data.Loc
import com.project.locarm.data.SelectDestination
import com.project.locarm.main.MainActivity
import com.project.locarm.main.MainActivity.Companion.NAME
import com.project.locarm.main.MainActivity.Companion.SELECT
import java.text.DecimalFormat

class BackgroundLocationUpdateService : Service() {
    private lateinit var context: Context
    private lateinit var realTimeLocation: RealTimeLocation
    private lateinit var notificationManager: NotificationManager
    private var startLocation: Loc? = null
    private var stopService = false
    private var destination: SelectDestination? = null

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
        destination = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra(SELECT, SelectDestination::class.java)
        } else {
            intent?.getParcelableExtra(SELECT)
        }

        startForeground()

        realTimeLocation.getLocation(
            destination!!,
            { distance, latitude, longitude -> updateNotification(distance, latitude, longitude) },
            { distance -> vibrateWithAlarm(distance) }
        )

        return START_STICKY
    }

    private fun vibrateWithAlarm(distance: Int) {
        if (distance > MyApplication.prefs.getAlarmDistance(DISTANCE)) return
        PushAlarm.build(
            context,
            "목적지 인접",
            "목적지까지 ${DecimalFormat("##0.0").format(distance.toKm())}KM 남았습니다.",
            MyApplication.prefs.getAddress(NAME, "")!!
        )
        val vibrator: Vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(1000)
    }

    private fun Int.toKm(): Double = this.toDouble() / 1000

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
            NotificationManager.IMPORTANCE_LOW
        )
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        channel.enableVibration(false)
        notificationManager.createNotificationChannel(channel)
    }

    private fun updateNotification(distance: Int, latitude: Double, longitude: Double) {
        notificationManager.notify(101, getNotification(distance, latitude, longitude))
    }

    private fun getNotification(
        distance: Int = MyApplication.prefs.getAlarmDistance(DISTANCE),
        testLatitude: Double = 0.0,
        testLongitude: Double = 0.0
    ): Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0 /* Request code */,
            intent,
            PendingIntent.FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT
        )

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
        Log.e("TotalDistance", totalDistance.toString())
        val foreGroundLayout =
            RemoteViews(packageName, R.layout.location_foreground_layout).apply {
                setTextViewText(R.id.distance_tv, DecimalFormat("##0.0").format(distance.toKm()))
                setProgressBar(
                    R.id.distance_progress_bar,
                    totalDistance,
                    totalDistance - distance,
                    false
                )

                setTextViewText(
                    R.id.destination_guide_tv,
                    "${MyApplication.prefs.getAddress(NAME, "목적지")}까지 남은 거리"
                )

                setTextViewText(R.id.test_latitude, "$testLatitude")
                setTextViewText(R.id.test_longitude, "$testLongitude")
            }

        builder
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(foreGroundLayout)
            .setContentIntent(pendingIntent)
            .setContentTitle("위치 정보")
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_launcher_background)

        return builder.build()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        Log.e(TAG, "Service Stopped")
        realTimeLocation.onDestroy()
        stopService = true
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL_ID = "channel_location"
        private const val CHANNEL_NAME = "channel_location"
        private const val TAG = "BackgroundLocationUpdateService"
    }
}
