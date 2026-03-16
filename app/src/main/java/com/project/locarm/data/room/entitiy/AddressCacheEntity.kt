package com.project.locarm.data.room.entitiy

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class AddressRemoteKey(
    @PrimaryKey val query: String,
    val nextKey: Int?
)

@Entity
data class AddressEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val query: String,
    val jibunAddr: String,
    val roadAddr: String,
    val name: String
)
