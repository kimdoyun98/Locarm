package com.project.locarm.common

import android.app.Application
import com.naver.maps.map.NaverMapSdk
import com.project.locarm.BuildConfig

class MyApplication : Application() {
    companion object {
        lateinit var prefs: PreferenceUtil
        lateinit var instance: MyApplication
        lateinit var serviceLocator: ServiceLocator
    }

    override fun onCreate() {
        prefs = PreferenceUtil(applicationContext)
        super.onCreate()

        instance = this
        serviceLocator = ServiceLocator(this)

        NaverMapSdk.getInstance(this).client =
            NaverMapSdk.NaverCloudPlatformClient(BuildConfig.Client_ID)
    }
}
