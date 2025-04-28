package com.project.locarm.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.project.locarm.common.MyApplication
import com.project.locarm.common.PreferenceUtil.Companion.DISTANCE
import com.project.locarm.data.room.DataBase
import com.project.locarm.data.room.Favorite
import com.project.locarm.data.room.FavoritesDao
import kotlinx.coroutines.launch

class MainViewModel(
    private val dao: FavoritesDao
) : ViewModel() {

    private val _alarmStatus = MutableLiveData<Boolean>().apply { value = false }
    val alarmStatus: LiveData<Boolean> = _alarmStatus

    private val _distance = MutableLiveData<Int>().apply { value = MyApplication.prefs.getAlarmDistance(DISTANCE)/1000 }
    val distance: LiveData<Int> = _distance

    private val _address = MutableLiveData<String>().apply { value = "목적지" }
    var address: LiveData<String> = _address

    val favoriteList: LiveData<List<Favorite>> = dao.getAll()

    fun setAddress(address: String?){
        if(address == null) return
        _address.value = address
    }

    fun alarmCheck() {
        _alarmStatus.value = !alarmStatus.value!!
    }

    fun allDelete() {
        viewModelScope.launch {
            dao.deleteAll()
        }
    }

    fun delete(id: Int) {
        viewModelScope.launch {
            dao.delete(id)
        }
    }

    fun refreshDistance(){
        _distance.value = MyApplication.prefs.getAlarmDistance(DISTANCE)/1000
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                return MainViewModel(
                    MyApplication.serviceLocator.dao
                ) as T
            }
        }
    }
}
