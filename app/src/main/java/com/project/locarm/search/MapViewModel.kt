package com.project.locarm.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.project.locarm.data.AddressDTO

class MapViewModel : ViewModel() {
    private var _address = MutableLiveData<AddressDTO.Result.Juso>()
    val address : LiveData<AddressDTO.Result.Juso> = _address

    fun setData(data: AddressDTO.Result.Juso){
        _address.postValue(data)
    }
}