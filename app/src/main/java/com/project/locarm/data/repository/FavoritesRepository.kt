package com.project.locarm.data.repository

import androidx.lifecycle.LiveData
import com.project.locarm.data.datasource.FavoritesDataSource
import com.project.locarm.data.room.Favorite

class FavoritesRepository(
    private val favoritesDataSource: FavoritesDataSource
) {
    fun getFavorite(name: String): LiveData<Favorite> {
        return favoritesDataSource.getFavorite(name)
    }

    fun getAllFavorites(): LiveData<List<Favorite>> {
        return favoritesDataSource.getAllFavorites()
    }

    suspend fun deleteAllFavorites() {
        favoritesDataSource.deleteAllFavorites()
    }

    suspend fun deleteFavorite(id: Int) {
        favoritesDataSource.deleteFavorite(id)
    }

    suspend fun insertFavorite(favorite: Favorite) {
        favoritesDataSource.insertFavorite(favorite)
    }
}
