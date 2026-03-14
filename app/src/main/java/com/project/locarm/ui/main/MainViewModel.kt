package com.project.locarm.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.project.locarm.common.PreferenceUtil
import com.project.locarm.common.PreferenceUtil.Companion.DISTANCE
import com.project.locarm.data.model.SelectDestination
import com.project.locarm.data.repository.LocationRepository
import com.project.locarm.location.util.GeoCoder
import com.project.locarm.location.LocationObserver
import com.project.locarm.location.util.LocationState
import com.project.locarm.location.RealTimeLocation
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn

@OptIn(FlowPreview::class)
class MainViewModel(
    private val preference: PreferenceUtil,
    private val locationRepository: LocationRepository,
    private val locationObserver: LocationObserver,
    private val realTimeLocation: RealTimeLocation
) : ViewModel() {
    val locationState = locationObserver.observe
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            LocationState.Idle
        )

    private val _serviceState = MutableLiveData<ServiceState>().apply { value = ServiceState.Idle }
    val serviceState: LiveData<ServiceState> = _serviceState

    private val _trackingButtonClick = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val trackingButtonClick = _trackingButtonClick
        .debounce(200L)
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(500L)
        )

    private val _changeAlarmDistance = MutableStateFlow<Float?>(null)
    val alarmRangeDistance = _changeAlarmDistance
        .filterNotNull()
        .map { it.toInt() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(500L),
            initialValue = preference.getAlarmDistance(DISTANCE)
        )

    private val _destination = MutableStateFlow<SelectDestination?>(null)
    val destination = _destination.asStateFlow()

    private val _distanceRemaining = MutableStateFlow<Int>(0)
    val distanceRemaining = _distanceRemaining
        .map {
            GeoCoder.getDistanceKmToString(it)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(500L),
            initialValue = "0"
        )
    val updateDistanceRemaining = destination
        .filterNotNull()
        .combine(locationState) { destination, state ->
            destination to state
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(500L),
            initialValue = null to LocationState.Idle
        )


    val updateAlarmRangeDistance = { value: Float -> _changeAlarmDistance.value = value }
    val destinationNearbyAlarm = locationRepository.destinationNearbyAlarm

    init {
        locationRepository.distanceRemaining
            .onEach {
                _distanceRemaining.value = it
            }
            .launchIn(viewModelScope)

        alarmRangeDistance
            .debounce(300L)
            .onEach {
                preference.setAlarmDistance(DISTANCE, it)
            }
            .launchIn(viewModelScope)
    }

    fun onClickTrackingButton() {
        when (locationState.value) {
            LocationState.Idle -> Unit

            LocationState.PermissionDenied -> {
                _trackingButtonClick.tryEmit(LOCATION_PERMISSION_DENIED)
            }

            LocationState.LocationDisabled -> {
                _trackingButtonClick.tryEmit(LOCATION_DISABLED)
            }

            LocationState.Ready -> {
                _trackingButtonClick.tryEmit(SERVICE_READY)
            }
        }
    }

    fun isGrantedLocationPermission(state: LocationState) {
        locationObserver.locationPermissionUpdate(state)
    }

    fun getDistanceRemainingInteger() = _distanceRemaining.value

    fun setDestination(destination: SelectDestination?) {
        _destination.value = destination
    }

    fun setServiceState(serviceState: ServiceState) {
        _serviceState.value = serviceState
    }

    fun updateDistanceRemaining(distance: Int) {
        _distanceRemaining.value = distance
    }

    companion object {
        const val LOCATION_PERMISSION_DENIED = 0
        const val LOCATION_DISABLED = 1
        const val SERVICE_READY = 2

        fun factory(
            preference: PreferenceUtil,
            locationRepository: LocationRepository,
            locationObserver: LocationObserver,
            realTimeLocation: RealTimeLocation,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                return MainViewModel(
                    preference,
                    locationRepository,
                    locationObserver,
                    realTimeLocation
                ) as T
            }
        }
    }
}
