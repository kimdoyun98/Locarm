package com.project.locarm.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddressDTO(
    @SerialName("results")
    val result: Result
)

@Serializable
data class Result(
    @SerialName("common")
    val common: Common,

    @SerialName("juso")
    val juso: List<Juso> = emptyList(),
)

@Serializable
data class Common(
    @SerialName("errorMessage")
    val errorMessage: String,

    @SerialName("errorCode")
    val errorCode: String,
)

@Serializable
data class Juso(
    @SerialName("jibunAddr")
    val jibunAddr: String,

    @SerialName("roadAddr")
    val roadAddr: String,

    @SerialName("bdNm")
    val name: String
)
