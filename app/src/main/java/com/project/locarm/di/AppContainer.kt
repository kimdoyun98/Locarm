package com.project.locarm.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.project.locarm.common.PreferenceUtil
import com.project.locarm.common.ads.InterstitialAdManager
import com.project.locarm.data.datasource.FavoritesDataSource
import com.project.locarm.data.remote.ApiService
import com.project.locarm.data.remote.RetrofitManager
import com.project.locarm.data.repository.AddressRepository
import com.project.locarm.data.repository.AdsRepository
import com.project.locarm.data.repository.FavoritesRepository
import com.project.locarm.data.repository.LocationRepository
import com.project.locarm.data.room.DataBase
import com.project.locarm.data.room.DatabaseMigrations
import com.project.locarm.location.LocationObserver
import com.project.locarm.location.RealTimeLocation

class AppContainer(context: Context) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = ADS_PREFERENCES_NAME
    )
    private val retrofitManager = RetrofitManager.getRetrofitInstance()
    private val database by lazy {
        Room.databaseBuilder(
            context,
            DataBase::class.java,
            "db"
        )
            .addMigrations(DatabaseMigrations.MIGRATION_3_4)
            .fallbackToDestructiveMigration(false)
            .build()
    }

    private val favoritesDao by lazy { database.favoriteDao() }
    private val addressEntityDao by lazy { database.addressEntityDao() }
    private val addressRemoteKeyDao by lazy { database.addressRemoteKeyDao() }
    private val favoritesDataSource by lazy { FavoritesDataSource(favoritesDao) }

    val addressRepository by lazy {
        AddressRepository(
            service = retrofitManager.create(ApiService::class.java),
            database = database,
            addressEntityDao = addressEntityDao,
            addressRemoteKeyDao = addressRemoteKeyDao
        )
    }
    val favoritesRepository by lazy { FavoritesRepository(favoritesDataSource) }
    val locationRepository by lazy { LocationRepository() }
    val adsRepository by lazy { AdsRepository(context.dataStore) }
    val realTimeLocation by lazy { RealTimeLocation(context) }
    val preference by lazy { PreferenceUtil(context) }
    val locationObserver by lazy { LocationObserver(context) }
    val interstitialAdManager by lazy { InterstitialAdManager() }

    companion object {
        private const val ADS_PREFERENCES_NAME = "Ads"
    }
}
