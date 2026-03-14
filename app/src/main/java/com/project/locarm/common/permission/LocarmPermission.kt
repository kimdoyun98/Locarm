package com.project.locarm.common.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.project.locarm.R
import com.project.locarm.location.util.LocationState
import com.project.locarm.ui.view.LocarmSnackBar

class LocarmPermission(
    private val activity: ComponentActivity,
    isGrantedAction: (LocationState) -> Unit,
) {
    private val permissionsLauncher =
        activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val deniedList = permissions.entries
                .filter { (permission, isGranted) ->
                    if (permission == LOCATION_PERMISSION && isGranted) {
                        isGrantedAction(LocationState.Ready)
                    }
                    !isGranted
                }
                .map { (permission, isGranted) ->
                    permission
                }

            if (deniedList.isEmpty()) return@registerForActivityResult

            if (deniedList.size == 3) {
                showSnackBar(
                    message = activity.getString(R.string.all_permission_denied),
                    actionText = activity.getString(R.string.permission_request),
                    action = { requestAllPermission() }
                )
            } else if (deniedList.contains(NOTIFICATION_PERMISSION)) {
                if (!activity.shouldShowRequestPermissionRationale(NOTIFICATION_PERMISSION)) {
                    showSnackBar(
                        message = activity.getString(R.string.never_notification_permission_granted),
                        actionText = activity.getString(R.string.setting),
                        action = { moveSetting() }
                    )
                } else {
                    showSnackBar(
                        message = activity.getString(R.string.notification_permission_denied),
                        actionText = activity.getString(R.string.permission_request),
                        action = { requestAllPermission() }
                    )
                }
            } else {
                if (!activity.shouldShowRequestPermissionRationale(LOCATION_PERMISSION)) {
                    showSnackBar(
                        message = activity.getString(R.string.never_location_permission_granted),
                        actionText = activity.getString(R.string.setting),
                        action = { moveSetting() }
                    )
                } else {
                    showSnackBar(
                        message = activity.getString(R.string.location_permission_denied),
                        actionText = activity.getString(R.string.permission_request),
                        action = { requestAllPermission() }
                    )
                }
            }
        }

    private val permissionArray =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            locationPermissions + NOTIFICATION_PERMISSION
        } else {
            locationPermissions
        }

    fun checkLocationPermission(): Boolean =
        checkPermission(activity, locationPermissions)

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun checkNotificationPermission(): Boolean =
        checkPermission(activity, arrayOf(NOTIFICATION_PERMISSION))


    fun requestAllPermission() {
        permissionsLauncher.launch(permissionArray)
    }

    fun requestLocationPermission() {
        permissionsLauncher.launch(locationPermissions)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun requestNotificationPermission() {
        permissionsLauncher.launch(arrayOf(NOTIFICATION_PERMISSION))
    }

    private fun moveSetting() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", activity.packageName, null)
        }
        activity.startActivity(intent)
    }

    private fun showSnackBar(message: String, actionText: String, action: () -> Unit) {
        LocarmSnackBar
            .make(activity, message, LocarmSnackBar.LONG)
            .setAction(
                text = actionText,
                onClick = action
            )
            .show()
    }

    companion object {
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        const val NOTIFICATION_PERMISSION = Manifest.permission.POST_NOTIFICATIONS
        const val LOCATION_PERMISSION = Manifest.permission.ACCESS_COARSE_LOCATION
        const val EXACT_LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION
        private val locationPermissions = arrayOf(
            LOCATION_PERMISSION,
            EXACT_LOCATION_PERMISSION
        )

        fun checkLocationPermission(context: Context): Boolean =
            checkPermission(context, locationPermissions)

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        fun checkNotificationPermission(context: Context): Boolean =
            checkPermission(context, arrayOf(NOTIFICATION_PERMISSION))

        fun checkTiramisuVersionHigher(): Boolean =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

        private fun checkPermission(context: Context, permissions: Array<String>): Boolean {
            return permissions.all {
                ContextCompat.checkSelfPermission(
                    context,
                    it
                ) == PackageManager.PERMISSION_GRANTED
            }
        }
    }
}
