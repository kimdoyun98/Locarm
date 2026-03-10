package com.project.locarm.ui.favorite

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.project.locarm.data.model.SelectDestination
import com.project.locarm.data.repository.FavoritesRepository
import com.project.locarm.data.room.Favorite
import com.project.locarm.ui.favorite.util.FavoriteLongClickState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FavoriteViewModel(
    private val favoritesRepository: FavoritesRepository,
) : ViewModel() {
    val favoriteList: LiveData<List<Favorite>> = favoritesRepository.getAllFavorites()

    private val _selectDestinationFavorite = MutableLiveData<SelectDestination?>(null)
    val selectDestinationFavorite: LiveData<SelectDestination?> = _selectDestinationFavorite

    private val selectedItemList = mutableListOf<Int>()
    private val _selectedItems = MutableLiveData<List<Int>>(emptyList())
    val selectedItems: LiveData<List<Int>> = _selectedItems

    private val _longClickState =
        MutableStateFlow<FavoriteLongClickState>(FavoriteLongClickState.Idle)
    val longClickState = _longClickState.asStateFlow()

    fun itemLongClick(id: Int): Boolean {
        _longClickState.value = FavoriteLongClickState.LongClickState
        selectedItemList.add(id)
        _selectedItems.value = selectedItemList.toList()

        return true
    }

    fun itemClick(id: Int) {
        when (longClickState.value) {
            is FavoriteLongClickState.Idle -> {
                val list = favoriteList.value ?: emptyList()
                val item = list[id]

                _selectDestinationFavorite.value = SelectDestination(
                    name = item.name,
                    latitude = item.latitude,
                    longitude = item.longitude
                )
            }

            is FavoriteLongClickState.LongClickState -> {
                if (selectedItemList.contains(id)) selectedItemList.remove(id)
                else selectedItemList.add(id)

                _selectedItems.value = selectedItemList.toList()
            }
        }
    }

    fun cancelLongClick() {
        _longClickState.value = FavoriteLongClickState.Idle
        selectedItemList.clear()

        _selectedItems.value = selectedItemList.toList()
    }

    fun selectItemAll() {
        val list = favoriteList.value ?: emptyList()

        if (selectedItemList.size == list.size) {
            selectedItemList.clear()
        } else {
            selectedItemList.clear()
            selectedItemList.addAll((0 until list.size).toList())
        }

        _selectedItems.value = selectedItemList.toList()
    }

    fun delete() {
        viewModelScope.launch {
            val deleteIndexList = selectedItemList.toList()

            selectedItemList.clear()
            _selectedItems.value = selectedItemList.toList()

            deleteIndexList.forEach { index ->
                launch {
                    val item = favoriteList.value!![index]
                    favoritesRepository.deleteFavorite(item.id)
                }
            }

            _longClickState.value = FavoriteLongClickState.Idle
        }
    }

    companion object {
        fun factory(
            favoritesRepository: FavoritesRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                return FavoriteViewModel(
                    favoritesRepository,
                ) as T
            }
        }
    }
}
