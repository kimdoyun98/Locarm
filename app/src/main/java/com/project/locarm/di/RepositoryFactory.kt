package com.project.locarm.di

import com.project.locarm.data.remote.ApiService
import com.project.locarm.data.repository.AddressRepository
import com.project.locarm.data.repository.FavoritesRepository

object RepositoryFactory {
    private val addressApi: ApiService = RetrofitFactory.createAddressApi()

    fun createAddressRepository(): AddressRepository = AddressRepository(addressApi)

    fun createFavoritesRepository(): FavoritesRepository =
        FavoritesRepository(DataSourceFactory.createFavoritesDataSource())
}
