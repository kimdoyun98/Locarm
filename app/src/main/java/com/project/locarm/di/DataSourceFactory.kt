package com.project.locarm.di

import com.project.locarm.data.datasource.FavoritesDataSource

object DataSourceFactory {
    private val favoritesDao = DaoFactory.createFavoriteDao()
    private val favoritesDataSource: FavoritesDataSource = FavoritesDataSource(favoritesDao)

    fun createFavoritesDataSource(): FavoritesDataSource = favoritesDataSource
}
