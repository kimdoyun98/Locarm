package com.project.locarm.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.naver.maps.map.util.FusedLocationSource
import com.project.locarm.common.MyApplication
import com.project.locarm.data.Juso
import com.project.locarm.data.Loc
import com.project.locarm.data.SelectDestination
import com.project.locarm.data.repository.AddressRepository
import com.project.locarm.data.repository.FavoritesRepository
import com.project.locarm.data.room.Favorite
import kotlinx.coroutines.launch

class SearchViewModel(
    private val favoritesRepository: FavoritesRepository,
    private val addressRepository: AddressRepository
) : ViewModel() {
    private var _address = MutableLiveData<Juso>()
    val address: LiveData<Juso> = _address

    var selectDestination: SelectDestination? = null

    lateinit var locationSource: FusedLocationSource
    lateinit var location: Loc
    lateinit var keyword: String

    fun searchAddress(keyword: String) = addressRepository.getSearchResultStream(keyword)

    fun setAddressJuso(data: Juso) {
        _address.postValue(data)
    }

    fun getFavorite(name: String): LiveData<Favorite> {
        return favoritesRepository.getFavorite(name)
    }

    fun insertFavorite(favorite: Favorite) {
        viewModelScope.launch {
            favoritesRepository.insertFavorite(favorite)
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
                    MyApplication.serviceLocator.favoritesRepository,
                    MyApplication.serviceLocator.addressRepository
                ) as T
            }
        }
    }
}
