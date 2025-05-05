package com.project.locarm.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.project.locarm.common.MyApplication
import com.project.locarm.common.PreferenceUtil.Companion.DISTANCE
import com.project.locarm.data.SelectDestination
import com.project.locarm.data.repository.FavoritesRepository
import com.project.locarm.data.room.Favorite
import kotlinx.coroutines.launch

class MainViewModel(
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val _serviceState = MutableLiveData<ServiceState>().apply { value = ServiceState.Idle }
    val serviceState: LiveData<ServiceState> = _serviceState

    fun setServiceState(serviceState: ServiceState) {
        _serviceState.value = serviceState
    }

    private val _distance = MutableLiveData<Int>().apply {
        value = MyApplication.prefs.getAlarmDistance(DISTANCE) / 1000
    }
    val distance: LiveData<Int> = _distance

    private val _destination = MutableLiveData<SelectDestination?>().apply { value = null }
    val destination: LiveData<SelectDestination?> = _destination

    val favoriteList: LiveData<List<Favorite>> = favoritesRepository.getAllFavorites()

    fun setDestination(destination: SelectDestination?) {
        _destination.value = destination
    }

    fun allDelete() {
        viewModelScope.launch {
            favoritesRepository.deleteAllFavorites()
        }
    }

    fun delete(id: Int) {
        viewModelScope.launch {
            favoritesRepository.deleteFavorite(id)
        }
    }

    fun refreshDistance() {
        _distance.value = MyApplication.prefs.getAlarmDistance(DISTANCE) / 1000
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                return MainViewModel(
                    MyApplication.serviceLocator.favoritesRepository
                ) as T
            }
        }
    }
}
