package com.project.locarm.location

import android.content.Context
import android.location.Geocoder
import android.location.Location
import java.text.DecimalFormat
import java.util.Locale

object GeoCoder {
    private const val KM_FORMAT_PATTERN = "##0.0"
    fun getXY(context: Context, address: String): Location {
        return try {
            Geocoder(context, Locale.KOREA).getFromLocationName(address, 1)?.let {
                Location("").apply {
                    latitude = it[0].latitude
                    longitude = it[0].longitude
                }
            } ?: Location("").apply {
                latitude = 0.0
                longitude = 0.0
            }
        } catch (e: Exception) {
            e.printStackTrace()
            getXY(context, address) //재시도
        }
    }

    fun getDistanceKmToString(distance: Int): String {
        return DecimalFormat(KM_FORMAT_PATTERN).format(distance.toKm())
    }

    private fun Int.toKm(): Double = this.toDouble() / 1000
}