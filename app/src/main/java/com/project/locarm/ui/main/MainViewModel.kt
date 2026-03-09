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
import com.project.locarm.di.PreferenceManager
import com.project.locarm.di.RepositoryFactory
import com.project.locarm.location.GeoCoder
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn

class MainViewModel(
    private val preference: PreferenceUtil,
    private val locationRepository: LocationRepository,
) : ViewModel() {

    private val _serviceState = MutableLiveData<ServiceState>().apply { value = ServiceState.Idle }
    val serviceState: LiveData<ServiceState> = _serviceState

    private val _changeAlarmDistance = MutableStateFlow<Float?>(null)

    private val _trackingButtonClick = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)
    val trackingButtonClick = _trackingButtonClick
        .debounce(200L)
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(500L)
        )

    @OptIn(FlowPreview::class)
    val alarmRangeDistance = _changeAlarmDistance
        .filterNotNull()
        .debounce(300L)
        .map { it.toInt() }
        .onEach {
            preference.setAlarmDistance(DISTANCE, it)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(500L),
            initialValue = preference.getAlarmDistance(DISTANCE)
        )

    private val _destination = MutableLiveData<SelectDestination?>().apply { value = null }
    val destination: LiveData<SelectDestination?> = _destination

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

    val updateAlarmRangeDistance = { value: Float -> _changeAlarmDistance.value = value }

    init {
        locationRepository.distanceRemaining
            .onEach {
                _distanceRemaining.value = it
            }
            .launchIn(viewModelScope)
    }

    fun onClickTrackingButton() {
        _trackingButtonClick.tryEmit(true)
    }

    fun getDistanceRemainingInteger() = _distanceRemaining.value

    fun setDestination(destination: SelectDestination?) {
        _destination.value = destination
    }

    fun setServiceState(serviceState: ServiceState) {
        _serviceState.value = serviceState
    }

    fun setDistanceRemaining(distance: Int) {
        _distanceRemaining.value = distance
    }

    fun updateDistanceRemaining(distance: Int) {

        _distanceRemaining.value = distance
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                return MainViewModel(
                    PreferenceManager.get(),
                    RepositoryFactory.createLocationRepository(),
                ) as T
            }
        }
    }
}
