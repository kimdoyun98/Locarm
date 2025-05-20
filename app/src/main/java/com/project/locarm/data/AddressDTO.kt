package com.project.locarm.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddressDTO(
    @SerialName("results")
    val result: Result
)

@Serializable
data class Result(
    @SerialName("juso")
    val juso: List<Juso>,
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
