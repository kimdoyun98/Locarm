package com.project.locarm.common

import com.project.locarm.data.datasource.FavoritesDataSource
import com.project.locarm.data.remote.ApiService
import com.project.locarm.data.remote.RetrofitManager
import com.project.locarm.data.repository.AddressRepository
import com.project.locarm.data.repository.FavoritesRepository
import com.project.locarm.data.room.DataBase

class ServiceLocator(
    private val application: MyApplication
) {
    private val database = DataBase.getInstance(application)!!
    private val dao = database.favoriteDao()

    private val retrofitManager = RetrofitManager.getRetrofitInstance()
    val addressApi: ApiService = retrofitManager.create(ApiService::class.java)

    val addressRepository = AddressRepository(addressApi)

    private val favoritesDataSource = FavoritesDataSource(dao)
    val favoritesRepository = FavoritesRepository(favoritesDataSource)
}
