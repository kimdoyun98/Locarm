package com.project.locarm.common

import android.app.Application
import com.naver.maps.map.NaverMapSdk
import com.project.locarm.BuildConfig
import com.project.locarm.di.LocationFactory
import com.project.locarm.di.PreferenceManager

class MyApplication : Application() {
    companion object {
        lateinit var instance: MyApplication
    }

    override fun onCreate() {
        PreferenceManager.init(applicationContext)
        LocationFactory.init(applicationContext)
        super.onCreate()

        instance = this

        NaverMapSdk.getInstance(this).client =
            NaverMapSdk.NaverCloudPlatformClient(BuildConfig.Client_ID)
    }
}
