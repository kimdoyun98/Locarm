package com.project.locarm

import android.annotation.SuppressLint
import android.app.*
import android.app.PendingIntent.FLAG_ONE_SHOT
import android.content.Context
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.location.Location
import android.location.LocationListener
import android.media.RingtoneManager
import android.net.Uri
import android.os.*
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.project.locarm.common.MyApplication
import com.project.locarm.common.PushAlarm
import com.project.locarm.main.MainActivity
import java.lang.Math.*
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit
import kotlin.math.pow
import kotlin.math.roundToLong


class BackgroundLocationUpdateService : Service(),
    GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    /**
     * Declare in manifest
     * <service android:name=".BackgroundLocationUpdateService"/>
     */

    private val TAG = "BackgroundLocationUpdateService"
    private val TAG_LOCATION = "TAG_LOCATION"
    private lateinit var context: Context
    private var stopService = false

    /** For Google Fused API **/
    protected var mGoogleApiClient: GoogleApiClient? = null
    protected var mLocationSettingsRequest: LocationSettingsRequest? = null
    private var latitude = "0.0"
    private var longitude = "0.0"
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mSettingsClient: SettingsClient? = null
    private var mLocationCallback: LocationCallback? = null
    private var mLocationRequest: LocationRequest? = null
    private var mCurrentLocation: Location? = null

    /** For Google Fused API **/

    override fun onCreate() {
        super.onCreate()
        context = this
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        StartForeground()
        val handler = Handler()
        val runnable: Runnable = object : Runnable {
            override fun run() {
                try {
                    if (!stopService) {
                        val latitude2 = MyApplication.prefs.getLocation("latitude", 0.0)!!
                        val longitude2 = MyApplication.prefs.getLocation("longitude", 0.0)!!
                        val distance = getDistance(latitude.toDouble(), longitude.toDouble(), latitude2.toDouble(), longitude2.toDouble())

                        /** 목적지까지 설정 값 이내면 알람 및 진동 **/
                        if(distance <= MyApplication.prefs.getAlarmDistance("distance")){
                            PushAlarm.build(
                                context,
                                "목적지 인접",
                                "목적지까지 ${DecimalFormat("##0.0").format(distance.toDouble()/1000)}KM 남았습니다.",
                                MyApplication.prefs.getAddress("name", "")!!
                            )
                            val vibrator : Vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                            vibrator.vibrate(1000)
                        }

                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    if (!stopService) {
                        handler.postDelayed(this, TimeUnit.SECONDS.toMillis(10))
                    }
                }
            }
        }
        handler.postDelayed(runnable, 2000)
        buildGoogleApiClient()
        return START_STICKY
    }

    override fun onDestroy() {
        Log.e(TAG, "Service Stopped")
        stopService = true
        if (mFusedLocationClient != null) {
            mFusedLocationClient!!.removeLocationUpdates(mLocationCallback!!)
            Log.e(TAG_LOCATION, "Location Update Callback Removed")
        }
        super.onDestroy()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun StartForeground() {
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0 /* Request code */,
            intent,
            PendingIntent.FLAG_IMMUTABLE or FLAG_ONE_SHOT
        )
        val CHANNEL_ID = "channel_location"
        val CHANNEL_NAME = "channel_location"
        var builder: NotificationCompat.Builder? = null
        val notificationManager =
            applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            notificationManager.createNotificationChannel(channel)
            builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setChannelId(CHANNEL_ID)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
        }
        else {
            builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
        }

        val notificationSound: Uri =
            RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_NOTIFICATION)
        builder
            .setContentTitle("Your title")
            .setContentText("You are now online")
            .setSound(notificationSound)
            .setAutoCancel(true)
        //  .setSmallIcon(R.drawable.ic_logo)
            .setContentIntent(pendingIntent)

        val notification: Notification = builder.build()
        startForeground(101, notification)
    }

    override fun onLocationChanged(location: Location) {
        Log.e(
            TAG_LOCATION,
            "Location Changed Latitude : " + location.latitude + "\tLongitude : " + location.longitude
        )
        latitude = location.latitude.toString()
        longitude = location.longitude.toString()
        if (latitude.equals("0.0", ignoreCase = true) && longitude.equals(
                "0.0",
                ignoreCase = true
            )) {
            requestLocationUpdate()
        }
        else {
            Log.e(
                TAG_LOCATION,
                "Latitude : " + location.latitude + "\tLongitude : " + location.longitude
            )
        }
    }

    override fun onConnected(bundle: Bundle?) {
        mLocationRequest = LocationRequest()
            .setInterval((10 * 1000).toLong())
            .setFastestInterval((5 * 1000).toLong())
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

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

    @Synchronized
    protected fun buildGoogleApiClient() {
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

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdate() {
        mFusedLocationClient!!.requestLocationUpdates(
            mLocationRequest!!,
            mLocationCallback!!, Looper.myLooper()!!
        )
    }


    /**
     * 현재 위치에서 목적지까지의 거리
     */
    fun getDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double):Int{
        val R = 6372.8 * 1000
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) + sin(dLon / 2).pow(2.0) * cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2))
        val c = 2 * asin(sqrt(a))
        return (R * c).toInt()
    }
}