package com.project.locarm.di

import android.content.Context
import com.project.locarm.common.PreferenceUtil

object PreferenceManager {
    private lateinit var preference: PreferenceUtil

    fun init(context: Context) {
        preference = PreferenceUtil(context)
    }

    fun get(): PreferenceUtil = preference
}
