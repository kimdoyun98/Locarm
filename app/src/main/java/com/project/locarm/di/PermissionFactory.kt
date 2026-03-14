package com.project.locarm.di

import androidx.activity.ComponentActivity
import com.project.locarm.common.permission.LocarmPermission
import com.project.locarm.location.LocationState

object PermissionFactory {

    fun createLocarmPermission(
        activity: ComponentActivity,
        isGrantedAction: (LocationState) -> Unit,
    ) = LocarmPermission(activity, isGrantedAction)
}
