package com.project.locarm.common

import android.content.Context
import android.content.SharedPreferences

class PreferenceUtil (context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("prefs_name", Context.MODE_PRIVATE)

    fun getAddress(key:String, defValue: String): String? {
        return prefs.getString(key, defValue)
    }

    fun getAlarmDistance(key:String): Int{
        return prefs.getInt(key, 1000)
    }

    fun setAlarmDistance(key:String, value:Int){
        prefs.edit().putInt(key, value).apply()
    }

    companion object{
        const val DISTANCE = "distance"
    }
}
