package com.project.locarm.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Favorite(
    @PrimaryKey(autoGenerate = true)
    val id : Int = 0,
    val name : String,
    val latitude : Double,
    val longitude : Double
)
