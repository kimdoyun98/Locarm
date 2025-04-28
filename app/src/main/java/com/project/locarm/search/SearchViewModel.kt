package com.project.locarm.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.naver.maps.map.util.FusedLocationSource
import com.project.locarm.common.MyApplication
import com.project.locarm.data.AddressDTO
import com.project.locarm.data.remote.ApiService
import com.project.locarm.room.Favorite
import com.project.locarm.room.FavoritesDao
import com.project.locarm.search.SearchActivity.Loc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchViewModel(
    private val dao: FavoritesDao,
    private val addressApi: ApiService
) : ViewModel() {
    private var _address = MutableLiveData<AddressDTO.Result.Juso>()
    val address: LiveData<AddressDTO.Result.Juso> = _address

    var selectAddress: String? = null

    lateinit var locationSource: FusedLocationSource
    lateinit var location: Loc
    lateinit var keyword: String

    private val _result = MutableLiveData<AddressDTO>()
    val result: LiveData<AddressDTO> = _result

    private val _back = MutableLiveData<Boolean>().apply { value = false }
    val back: LiveData<Boolean> = _back

    private val _next = MutableLiveData<Boolean>().apply { value = false }
    val next: LiveData<Boolean> = _next

    fun searchAddress(keyword: String, page: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val response = addressApi.getAddress(keyword, page)

            if (response.isSuccessful) {
                _result.postValue(response.body())
            }
        }
    }

    fun setData(data: AddressDTO.Result.Juso) {
        _address.postValue(data)
    }

    fun getFavorite(name: String): LiveData<Favorite> {
        return dao.getFavorite(name)
    }

    fun insertFavorite(favorite: Favorite) {
        viewModelScope.launch {
            dao.insert(favorite)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                return SearchViewModel(
                    MyApplication.serviceLocator.dao,
                    MyApplication.serviceLocator.addressApi
                ) as T
            }
        }
    }
}
