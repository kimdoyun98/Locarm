package com.project.locarm.common

import android.app.Application
import com.naver.maps.map.NaverMapSdk
import com.project.locarm.BuildConfig

class MyApplication : Application() {
    companion object {
        lateinit var prefs: PreferenceUtil
        lateinit var instance: MyApplication
    }

    override fun onCreate() {
        prefs = PreferenceUtil(applicationContext)
        super.onCreate()

        instance = this

        NaverMapSdk.getInstance(this).client =
            NaverMapSdk.NaverCloudPlatformClient(BuildConfig.Client_ID)
    }
}
