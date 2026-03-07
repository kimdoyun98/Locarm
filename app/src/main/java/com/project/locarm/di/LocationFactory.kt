package com.project.locarm.di

import android.annotation.SuppressLint
import android.content.Context
import com.project.locarm.location.RealTimeLocation

object LocationFactory {
    @SuppressLint("StaticFieldLeak")
    private lateinit var realTimeLocation: RealTimeLocation

    fun init(context: Context){
        realTimeLocation = RealTimeLocation(context)
    }

    fun createRealTimeLocation() = realTimeLocation
}
