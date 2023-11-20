package com.project.locarm.data

import com.google.gson.annotations.SerializedName

data class AddressDTO(
    @SerializedName("results") val result : Result
){
    data class Result(
        @SerializedName("juso") val juso : ArrayList<Juso>,
        @SerializedName("common") val common : ResultCommon
    ){
        data class Juso(
            @SerializedName("jibunAddr") val jibunAddr : String,
            @SerializedName("roadAddr") val roadAddr : String,
            @SerializedName("bdNm") val name: String
        )

        data class ResultCommon(
            @SerializedName("countPerPage") val countPerPage : String,
            @SerializedName("totalCount") val totalCount : String,
            @SerializedName("currentPage") val currentPage: String
        )
    }
}
