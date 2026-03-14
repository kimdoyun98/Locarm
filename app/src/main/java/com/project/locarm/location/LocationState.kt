package com.project.locarm.location

sealed interface LocationState {
    object Idle: LocationState

    data object PermissionDenied : LocationState

    data object LocationDisabled : LocationState

    data object Ready : LocationState
}
