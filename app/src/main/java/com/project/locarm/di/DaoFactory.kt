package com.project.locarm.di

import com.project.locarm.data.room.FavoritesDao

object DaoFactory {
    private val database = DatabaseFactory.createDatabase()
    fun createFavoriteDao(): FavoritesDao = database.favoriteDao()
}
