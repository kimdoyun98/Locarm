package com.project.locarm.data.repository

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class LocationRepository {
    private val _distanceRemaining = MutableStateFlow(0)
    val distanceRemaining = _distanceRemaining.asStateFlow()

    private val _destinationNearbyAlarm = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)
    val destinationNearbyAlarm = _destinationNearbyAlarm.asSharedFlow()

    fun updateDistance(distance: Int) {
        _distanceRemaining.value = distance
    }

    fun emitDestinationNearbyAlarm() {
        _destinationNearbyAlarm.tryEmit(true)
    }
}
