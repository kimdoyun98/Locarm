package com.project.locarm.di

import com.project.locarm.data.datasource.FavoritesDataSource

object DataSourceFactory {
    private val favoritesDao = DaoFactory.createFavoriteDao()

    fun createFavoritesDataSource(): FavoritesDataSource = FavoritesDataSource(favoritesDao)
}
