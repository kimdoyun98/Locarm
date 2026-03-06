package com.project.locarm.di

import com.project.locarm.data.remote.ApiService
import com.project.locarm.data.remote.RetrofitManager

object RetrofitFactory {
    private val retrofitManager = RetrofitManager.getRetrofitInstance()

    fun createAddressApi(): ApiService =
        retrofitManager.create(ApiService::class.java)
}
