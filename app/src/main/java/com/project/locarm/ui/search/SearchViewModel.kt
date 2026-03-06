package com.project.locarm.ui.search

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.project.locarm.data.model.Juso
import com.project.locarm.data.model.Loc
import com.project.locarm.data.model.SelectDestination
import com.project.locarm.data.repository.AddressRepository
import com.project.locarm.data.repository.FavoritesRepository
import com.project.locarm.data.room.Favorite
import com.project.locarm.di.RepositoryFactory
import com.project.locarm.ui.search.util.SelectDestinationState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(
    private val favoritesRepository: FavoritesRepository,
    private val addressRepository: AddressRepository
) : ViewModel() {
    private val _selectDestinationState: MutableStateFlow<SelectDestinationState> =
        MutableStateFlow(SelectDestinationState.Idle)
    val selectDestinationState = _selectDestinationState.asStateFlow()

    fun selectDestinationOnMap(latitude: Double, longitude: Double) {
        _selectDestinationState.value = SelectDestinationState.SelectOnMap(
            Loc(
                latitude = latitude,
                longitude = longitude
            )
        )
    }

    fun selectSearchResult(juso: Juso, geo: Location) {
        _selectDestinationState.value = SelectDestinationState.SelectSearchResult(
            juso = juso,
            result = SelectDestination(
                name = juso.name,
                latitude = geo.latitude,
                longitude = geo.longitude
            )
        )
    }


    fun searchAddress(keyword: String) = addressRepository.getSearchResultStream(keyword)

    fun getFavorite(name: String): Favorite? {
        return favoritesRepository.getFavorite(name).value
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
                    RepositoryFactory.createFavoritesRepository(),
                    RepositoryFactory.createAddressRepository()
                ) as T
            }
        }
    }
}
