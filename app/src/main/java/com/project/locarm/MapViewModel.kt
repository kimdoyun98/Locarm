package com.project.locarm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MapViewModel : ViewModel() {
    private var _address = MutableLiveData<AddressDTO.Result.Juso>()
    val address : LiveData<AddressDTO.Result.Juso> = _address

    fun setData(data:AddressDTO.Result.Juso){
        _address.postValue(data)
    }
}