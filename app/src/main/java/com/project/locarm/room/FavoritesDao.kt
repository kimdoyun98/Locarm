package com.project.locarm.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface FavoritesDao {
    @Query("SELECT * FROM favorite")
    fun getAll(): LiveData<List<Favorite>>

    @Query("SELECT * FROM favorite WHERE name = :name")
    fun getFavorite(name: String): LiveData<Favorite>

    @Insert
    suspend fun insert(favorite: Favorite)

    @Query("Delete From favorite WHERE id = :id")
    suspend fun delete(id : Int)

    @Query("Delete From favorite")
    suspend fun deleteAll()
}
