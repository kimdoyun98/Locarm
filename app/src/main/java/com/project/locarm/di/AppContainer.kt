package com.project.locarm.di

import android.content.Context
import androidx.room.Room
import com.project.locarm.common.PreferenceUtil
import com.project.locarm.data.datasource.FavoritesDataSource
import com.project.locarm.data.remote.ApiService
import com.project.locarm.data.remote.RetrofitManager
import com.project.locarm.data.repository.AddressRepository
import com.project.locarm.data.repository.FavoritesRepository
import com.project.locarm.data.repository.LocationRepository
import com.project.locarm.data.room.DataBase
import com.project.locarm.location.RealTimeLocation

class AppContainer(context: Context) {
    private val retrofitManager = RetrofitManager.getRetrofitInstance()
    private val database by lazy {
        Room.databaseBuilder(
            context,
            DataBase::class.java,
            "db"
        )
            .fallbackToDestructiveMigration(false)
            .build()
    }

    private val favoritesDao by lazy { database.favoriteDao() }
    private val favoritesDataSource by lazy { FavoritesDataSource(favoritesDao) }

    val addressRepository by lazy { AddressRepository(retrofitManager.create(ApiService::class.java)) }
    val favoritesRepository by lazy { FavoritesRepository(favoritesDataSource) }
    val locationRepository by lazy { LocationRepository() }
    val realTimeLocation by lazy { RealTimeLocation(context) }
    val preference by lazy { PreferenceUtil(context) }
}
