package com.project.locarm.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import com.project.locarm.common.permission.LocarmPermission
import com.project.locarm.location.util.LocationState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class LocationObserver(
    private val context: Context
) {
    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val _locationPermissionState: MutableStateFlow<LocationState> = MutableStateFlow(
        if (LocarmPermission.checkLocationPermission(context)) LocationState.Ready
        else LocationState.PermissionDenied
    )
    private val locationPermissionState = _locationPermissionState.asStateFlow()

    private val _locationEnabled = MutableStateFlow<LocationState>(
        if (isLocationEnabled()) LocationState.Ready
        else LocationState.LocationDisabled
    )
    private val locationEnabled = _locationEnabled.asStateFlow()
    val observe = locationEnabled
        .flatMapLatest { locationEnabledState ->
            locationPermissionState
                .map { permissionState ->
                    when (permissionState) {
                        LocationState.Ready -> {
                            locationEnabledState as? LocationState.LocationDisabled
                                ?: LocationState.Ready
                        }

                        else -> {
                            permissionState
                        }
                    }
                }
        }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
                _locationEnabled.value =
                    if (!isLocationEnabled()) LocationState.LocationDisabled
                    else LocationState.Ready
            }
        }
    }

    init {
        registerLocationEnabledReceiver()
    }

    fun locationPermissionUpdate(state: LocationState) {
        _locationPermissionState.value = state
    }

    fun unregisterLocationEnabledReceiver() {
        context.unregisterReceiver(receiver)
    }

    private fun registerLocationEnabledReceiver() {
        val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)

        context.registerReceiver(receiver, filter)
    }

    fun isLocationEnabled(): Boolean {

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
}
