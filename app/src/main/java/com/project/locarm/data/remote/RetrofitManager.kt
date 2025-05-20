package com.project.locarm.data.remote

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit

object RetrofitManager {
    private val contentType = "application/json".toMediaType()
    private const val URL = "https://business.juso.go.kr/addrlink/"
    private val json = Json { ignoreUnknownKeys = true }

    fun getRetrofitInstance(): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(json.asConverterFactory(contentType))
            .baseUrl(URL)
            .build()
    }
}
