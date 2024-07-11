package com.example.starterapplication.feature.deviceadmin.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.starterapplication.feature.deviceadmin.presentation.model.DeviceAdminState
import com.example.starterapplication.feature.deviceadmin.presentation.viewmodel.DeviceAdminViewModel

@Composable
fun DeviceAdminScreen(
    viewModel: DeviceAdminViewModel,
    onRequestDeviceAdmin: () -> Unit,
    onDeviceAdminActivated: () -> Unit
) {
    val state = viewModel.deviceAdminState.collectAsState()

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (val currentState = state.value) {
            is DeviceAdminState.Initial, is DeviceAdminState.Inactive -> {
                Button(onClick = onRequestDeviceAdmin) {
                    Text("Request Device Admin Permission")
                }
            }
            is DeviceAdminState.Loading -> {
                Text("Checking permission status...")
            }
            is DeviceAdminState.Requested -> {
                Text("Device Admin permission requested. Please approve in Settings.")
            }
            is DeviceAdminState.Active -> {
                Text("Device Admin permission is active!")
                onDeviceAdminActivated()
            }
            is DeviceAdminState.Error -> {
                Text("Error: ${currentState.message}")
            }
        }
    }
}
