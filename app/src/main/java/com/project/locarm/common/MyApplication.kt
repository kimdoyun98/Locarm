package com.project.locarm.common

import android.app.Application
import android.content.Context
import com.naver.maps.map.NaverMapSdk
import com.project.locarm.BuildConfig
import com.project.locarm.di.AppContainer

class MyApplication : Application() {
    val container by lazy { AppContainer(this) }

    companion object {
        lateinit var instance: MyApplication
    }

    override fun onCreate() {
        super.onCreate()

        instance = this

        NaverMapSdk.getInstance(this).client =
            NaverMapSdk.NaverCloudPlatformClient(BuildConfig.Client_ID)
    }
}

val Context.appContainer: AppContainer get() = (applicationContext as MyApplication).container
