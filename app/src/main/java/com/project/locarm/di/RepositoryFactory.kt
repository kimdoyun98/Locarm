package com.project.locarm.di

import com.project.locarm.data.remote.ApiService
import com.project.locarm.data.repository.AddressRepository
import com.project.locarm.data.repository.FavoritesRepository
import com.project.locarm.data.repository.LocationRepository

object RepositoryFactory {
    private val addressApi: ApiService = RetrofitFactory.createAddressApi()
    private val addressRepository: AddressRepository = AddressRepository(addressApi)
    private val favoritesRepository: FavoritesRepository =
        FavoritesRepository(DataSourceFactory.createFavoritesDataSource())
    private val locationRepository = LocationRepository()

    fun createAddressRepository(): AddressRepository = addressRepository

    fun createFavoritesRepository(): FavoritesRepository = favoritesRepository

    fun createLocationRepository(): LocationRepository = locationRepository
}
