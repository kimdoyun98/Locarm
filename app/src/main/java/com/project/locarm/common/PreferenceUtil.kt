package com.project.locarm.common

import android.content.Context
import android.content.SharedPreferences

class PreferenceUtil (context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("prefs_name", Context.MODE_PRIVATE)

    fun getBoolean(key: String, defValue: Boolean): Boolean {
        return prefs.getBoolean(key, defValue)
    }

    fun setBoolean(key: String, bool: Boolean) {
        prefs.edit().putBoolean(key, bool).apply()
    }

    fun getAddress(key:String, defValue: String): String? {
        return prefs.getString(key, defValue)
    }

    fun setAddress(key:String, value:String){
        prefs.edit().putString(key, value).apply()
    }
}