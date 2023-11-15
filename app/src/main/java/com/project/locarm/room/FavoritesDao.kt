package com.project.locarm.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface FavoritesDao {
    @Query("SELECT * FROM favorite")
    fun getAll(): LiveData<List<Favorite>>

    @Insert
    fun insert(favorite: Favorite)

    @Query("Delete From favorite WHERE id = :id")
    fun delete(id : Int)

    @Query("Delete From favorite")
    fun deleteAll()
}