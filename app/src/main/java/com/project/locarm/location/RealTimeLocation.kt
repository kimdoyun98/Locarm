package com.project.locarm.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.tasks.Task
import com.project.locarm.data.SelectDestination
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class RealTimeLocation(
    private val context: Context
) {
    private val mFusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private var locationRequest: LocationCallback? = null

    private val mLocationRequestHighAccuracy = LocationRequest.Builder(5_000L).apply {
        setPriority(PRIORITY_HIGH_ACCURACY)
        setIntervalMillis(5_000L)
        setMinUpdateIntervalMillis(5_000L)
    }.build()

    fun currentLocation(): Task<Location>? {
        return if (checkPermission()) mFusedLocationClient.lastLocation
        else null
    }

    fun getLocation(
        destination: SelectDestination,
        updateNotification: (Int, Double, Double) -> Unit,
        onVibrator: (Int) -> Unit
    ) {

        locationRequest =
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val lastLocation = locationResult.lastLocation!!
                    val distance = getDistance(
                        lastLocation.latitude,
                        lastLocation.longitude,
                        destination
                    )

                    updateNotification(distance, lastLocation.latitude, lastLocation.longitude)
                    onVibrator(distance)
                }
            }

        if (checkPermission()) {
            mFusedLocationClient.requestLocationUpdates(
                mLocationRequestHighAccuracy,
                locationRequest!!,
                Looper.myLooper()
            )
        }
    }

    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun getDistance(lat1: Double, lon1: Double, destination: SelectDestination): Int {

        val r = 6372.8 * 1000
        val dLat = Math.toRadians(destination.latitude - lat1)
        val dLon = Math.toRadians(destination.longitude - lon1)
        val a = sin(dLat / 2).pow(2.0) +
                sin(dLon / 2).pow(2.0) *
                cos(Math.toRadians(lat1)) *
                cos(Math.toRadians(destination.latitude))

        val c = 2 * asin(sqrt(a))
        return (r * c).toInt()
    }

    fun onDestroy() {
        mFusedLocationClient.removeLocationUpdates(locationRequest!!)
    }
}
