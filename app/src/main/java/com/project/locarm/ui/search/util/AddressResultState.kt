package com.project.locarm.ui.search.util

interface AddressResultState {
    object Idle: AddressResultState
    object Success: AddressResultState
    data class Error(val message: String = "Error"): AddressResultState
}
