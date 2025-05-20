package com.project.locarm.data.remote

import com.project.locarm.BuildConfig
import com.project.locarm.data.AddressDTO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("addrLinkApi.do?&countPerPage=10&resultType=json")
    suspend fun getAddress(
        @Query("keyword") keyword: String?,
        @Query("currentPage") page: Int,
        @Query("confmKey") confmkey: String = BuildConfig.Address_Key
    ): Response<AddressDTO>
}
