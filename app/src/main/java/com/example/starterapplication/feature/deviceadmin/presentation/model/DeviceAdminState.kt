package com.example.starterapplication.feature.deviceadmin.presentation.model

sealed class DeviceAdminState {
    data object Initial : DeviceAdminState()
    data object Loading : DeviceAdminState()
    data object Requested : DeviceAdminState()
    data object Active : DeviceAdminState()
    data object Inactive : DeviceAdminState()
    data class Error(val message: String) : DeviceAdminState()
}