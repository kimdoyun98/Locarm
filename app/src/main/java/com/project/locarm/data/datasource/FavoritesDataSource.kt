package com.project.locarm.data.datasource

import androidx.lifecycle.LiveData
import com.project.locarm.data.room.Favorite
import com.project.locarm.data.room.FavoritesDao

class FavoritesDataSource(
    private val dao: FavoritesDao
) {
    fun getFavorite(name: String): LiveData<Favorite> {
        return dao.getFavorite(name)
    }

    fun getAllFavorites(): LiveData<List<Favorite>> {
        return dao.getAll()
    }

    suspend fun deleteAllFavorites() {
        dao.deleteAll()
    }

    suspend fun deleteFavorite(id: Int){
        dao.delete(id)
    }

    suspend fun insertFavorite(favorite: Favorite){
        dao.insert(favorite)
    }
}
