package com.project.locarm.location

import android.Manifest
import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.widget.RemoteViews
import androidx.annotation.RequiresPermission
import com.project.locarm.R
import com.project.locarm.common.PreferenceUtil
import com.project.locarm.common.PreferenceUtil.Companion.DISTANCE
import com.project.locarm.common.appContainer
import com.project.locarm.common.permission.LocarmPermission
import com.project.locarm.data.model.Loc
import com.project.locarm.data.model.SelectDestination
import com.project.locarm.data.repository.LocationRepository
import com.project.locarm.location.util.GeoCoder
import com.project.locarm.ui.main.MainActivity.Companion.DISTANCE_REMAINING
import com.project.locarm.ui.main.MainActivity.Companion.SELECT

class BackgroundLocationUpdateService : Service() {
    private lateinit var pref: PreferenceUtil
    private val mBinder: IBinder = LocationServiceBind()
    private lateinit var realTimeLocation: RealTimeLocation
    private var startLocation: Loc? = null
    private var destination: SelectDestination? = null
    private val alarmDistance by lazy { pref.getAlarmDistance(DISTANCE) }
    private lateinit var locationRepository: LocationRepository
    private lateinit var locationNotification: LocationNotification

    inner class LocationServiceBind : Binder() {
        fun getDestination(): SelectDestination? = destination
    }

    override fun onCreate() {
        super.onCreate()
        realTimeLocation = applicationContext.appContainer.realTimeLocation
        pref = applicationContext.appContainer.preference
        locationRepository = applicationContext.appContainer.locationRepository
        locationNotification = LocationNotification(applicationContext, FOREGROUND_NOTIFICATION_ID)

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

        locationNotification.startVibrationNotification(
            title = getString(R.string.backgroundLocationUpdate_destination_nearby),
            content = getString(
                R.string.backgroundLocationUpdate_close_destination_notification_content,
                destination!!.name,
                GeoCoder.getDistanceKmToString(distance)
            )
        )
    }

    private fun startForeground() {
        startForeground(FOREGROUND_NOTIFICATION_ID, getNotification())
    }

    private fun updateNotification(distance: Int) {
        locationNotification.updateForegroundNotification(getNotification(distance))
    }

    private fun getNotification(
        distance: Int = pref.getAlarmDistance(DISTANCE)
    ): Notification {
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

        return locationNotification.getForegroundNotification(foreGroundLayout)
    }

    override fun onBind(p0: Intent?): IBinder {
        return mBinder
    }

    override fun onDestroy() {
        realTimeLocation.onDestroy()
        super.onDestroy()
    }

    companion object {
        private const val FOREGROUND_NOTIFICATION_ID = 101
        private const val DELETE = "delete"
    }
}
