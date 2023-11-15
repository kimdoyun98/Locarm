package com.project.locarm

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("addrLinkApi.do?currentPage=1&countPerPage=10&resultType=json")
    fun getAddress(@Query("keyword") keyword: String?,
                   @Query("confmKey") confmkey: String = BuildConfig.Address_Key): Call<AddressDTO>
}