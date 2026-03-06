package com.project.locarm.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class Loc(
    val latitude: Double,
    val longitude: Double
)

@Parcelize
data class SelectDestination(
    val name: String,
    val latitude: Double,
    val longitude: Double
) : Parcelable
