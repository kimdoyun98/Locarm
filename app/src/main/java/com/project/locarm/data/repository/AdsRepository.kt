package com.project.locarm.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.first

class AdsRepository(
    private val dataStore: DataStore<Preferences>,
) {
    suspend fun shouldShowAd(): Boolean {
        val preferences = dataStore.data.first()
        val last = preferences[LAST_AD_SHOWN_TIME] ?: 0L
        val current = System.currentTimeMillis()

        return current - last >= SIX_HOURS
    }


    suspend fun updateAdsLastTime(time: Long = System.currentTimeMillis()) {
        dataStore.edit { preferences ->
            preferences[LAST_AD_SHOWN_TIME] = time
        }
    }

    companion object {
        private val LAST_AD_SHOWN_TIME = longPreferencesKey("last_ad_shown_time")
        private const val SIX_HOURS = 6 * 60 * 60 * 1000L
    }
}
