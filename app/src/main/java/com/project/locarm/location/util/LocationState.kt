package com.project.locarm.location.util

sealed interface LocationState {
    object Idle: LocationState

    data object PermissionDenied : LocationState

    data object LocationDisabled : LocationState

    data object Ready : LocationState
}
