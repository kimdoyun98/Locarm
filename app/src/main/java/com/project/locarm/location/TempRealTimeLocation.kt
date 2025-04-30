package com.project.locarm.location

import android.Manifest
import android.content.Context
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.SettingsClient

class TempRealTimeLocation(
    private val context: Context,
    private val updateNotification: () -> Unit
): GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private val TAG_LOCATION = "TAG_LOCATION"

    /** For Google Fused API **/
    protected var mGoogleApiClient: GoogleApiClient? = null
    protected var mLocationSettingsRequest: LocationSettingsRequest? = null
    private var latitude = "0.0"
    private var longitude = "0.0"
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mSettingsClient: SettingsClient? = null
    private var mLocationCallback: LocationCallback? = null
    private var mLocationRequest: LocationRequest? = null // 필요
    private var mCurrentLocation: Location? = null

    fun getLatitude() = latitude
    fun getLongitude() = longitude

    override fun onConnected(p0: Bundle?) {
        mLocationRequest = LocationRequest.Builder(5_000L).apply {
            setPriority(PRIORITY_HIGH_ACCURACY)
            setIntervalMillis(5_000L)
            setMinUpdateIntervalMillis(5_000L)
        }.build()

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(mLocationRequest!!)
            .setAlwaysShow(true)

        mLocationSettingsRequest = builder.build()

        mSettingsClient!!
            .checkLocationSettings(mLocationSettingsRequest!!)
            .addOnSuccessListener {
                Log.e(TAG_LOCATION, "GPS Success")
                requestLocationUpdate()
            }.addOnFailureListener { e ->
                val statusCode = (e as ApiException).statusCode
                when (statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        val REQUEST_CHECK_SETTINGS = 214
                        val rae = e as ResolvableApiException
                        rae.startResolutionForResult(
                            (context as AppCompatActivity),
                            REQUEST_CHECK_SETTINGS
                        )
                    } catch (sie: SendIntentException) {
                        Log.e(TAG_LOCATION, "Unable to execute request.")
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> Log.e(
                        TAG_LOCATION,
                        "Location settings are inadequate, and cannot be fixed here. Fix in Settings."
                    )
                }
            }.addOnCanceledListener {
                Log.e(
                    TAG_LOCATION,
                    "checkLocationSettings -> onCanceled"
                )
            }
    }

    override fun onConnectionSuspended(p0: Int) {
        connectGoogleClient()
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        buildGoogleApiClient()
    }

    override fun onLocationChanged(location: Location) {
        Log.e(
            TAG_LOCATION,
            "Location Changed Latitude : " + location.latitude + "\tLongitude : " + location.longitude
        )
        latitude = location.latitude.toString()
        longitude = location.longitude.toString()

        if (latitude.equals("0.0", ignoreCase = true) &&
            longitude.equals("0.0", ignoreCase = true)) {
            requestLocationUpdate()
        }
        else {
            Log.e(
                TAG_LOCATION,
                "Latitude : " + location.latitude + "\tLongitude : " + location.longitude
            )
            updateNotification()
        }
    }

    @Synchronized
    fun buildGoogleApiClient() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        mSettingsClient = LocationServices.getSettingsClient(context)
        mGoogleApiClient = GoogleApiClient.Builder(context)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()
        connectGoogleClient()

        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                Log.e(TAG_LOCATION, "Location Received")
                mCurrentLocation = locationResult.lastLocation
                onLocationChanged(mCurrentLocation!!)
            }
        }
    }

    private fun connectGoogleClient() {
        val googleAPI = GoogleApiAvailability.getInstance()
        val resultCode = googleAPI.isGooglePlayServicesAvailable(context)
        if (resultCode == ConnectionResult.SUCCESS) {
            mGoogleApiClient!!.connect()
        }
    }

    private fun requestLocationUpdate() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient!!.requestLocationUpdates(
                mLocationRequest!!,
                mLocationCallback!!,
                Looper.myLooper()!!
            )
        }
    }

    fun onDestroy(){
        if (mFusedLocationClient != null) {
            mFusedLocationClient!!.removeLocationUpdates(mLocationCallback!!)
            Log.e(TAG_LOCATION, "Location Update Callback Removed")
        }
    }

}
