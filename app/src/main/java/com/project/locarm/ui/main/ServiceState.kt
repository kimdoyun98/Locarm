package com.project.locarm.ui.main

sealed interface ServiceState {
    object Idle: ServiceState
    object RunService: ServiceState
    object StopService: ServiceState
}
