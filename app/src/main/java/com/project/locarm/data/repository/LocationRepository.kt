package com.project.locarm.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocationRepository {
    private val _distanceRemaining = MutableStateFlow(0)
    val distanceRemaining = _distanceRemaining.asStateFlow()

    fun updateDistance(distance: Int) {
        _distanceRemaining.value = distance
    }
}
