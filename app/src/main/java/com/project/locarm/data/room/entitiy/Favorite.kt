package com.project.locarm.data.room.entitiy

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.project.locarm.data.model.SelectDestination

@Entity
data class Favorite(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double
)

fun SelectDestination.asEntity() = Favorite(
    name = name,
    latitude = latitude,
    longitude = longitude
)
