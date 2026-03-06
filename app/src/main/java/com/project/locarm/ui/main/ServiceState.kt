package com.project.locarm.ui.main

sealed interface ServiceState {
    data object Idle: ServiceState

    data class RunService(
        val onClick: () -> Unit
    ): ServiceState

    data class StopService(
        val onClick: () -> Unit
    ): ServiceState
}
