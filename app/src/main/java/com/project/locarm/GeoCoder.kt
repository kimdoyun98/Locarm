package com.project.locarm

import android.content.Context
import android.location.Geocoder
import android.location.Location
import java.util.*

object GeoCoder{
    fun getXY(context: Context, address:String): Location {
        return try {
            Geocoder(context, Locale.KOREA).getFromLocationName(address, 1)?.let{
                Location("").apply {
                    latitude =  it[0].latitude
                    longitude = it[0].longitude
                }
            }?: Location("").apply {
                latitude = 0.0
                longitude = 0.0
            }
        }catch (e:Exception) {
            e.printStackTrace()
            getXY(context, address) //재시도
        }
    }

}