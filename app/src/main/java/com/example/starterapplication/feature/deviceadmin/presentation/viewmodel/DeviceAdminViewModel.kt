package com.example.starterapplication.feature.deviceadmin.presentation.viewmodel

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.starterapplication.feature.deviceadmin.domain.DeviceAdminManager
import com.example.starterapplication.feature.deviceadmin.presentation.model.DeviceAdminState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DeviceAdminViewModel(private val deviceAdminManager: DeviceAdminManager) : ViewModel() {
    private val _deviceAdminState = MutableStateFlow<DeviceAdminState>(DeviceAdminState.Initial)
    val deviceAdminState: StateFlow<DeviceAdminState> = _deviceAdminState

    fun checkDeviceAdminStatus() {
        viewModelScope.launch {
            _deviceAdminState.value = if (deviceAdminManager.isDeviceAdminActive()) {
                DeviceAdminState.Active
            } else {
                DeviceAdminState.Inactive
            }
        }
    }

    fun requestDeviceAdmin(): Intent {
        _deviceAdminState.value = DeviceAdminState.Requested
        return deviceAdminManager.getDeviceAdminRequestIntent()
    }
}