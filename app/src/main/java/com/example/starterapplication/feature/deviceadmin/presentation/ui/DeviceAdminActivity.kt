package com.example.starterapplication.feature.deviceadmin.presentation.ui

import android.app.admin.DevicePolicyManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.example.starterapplication.MainActivity
import com.example.starterapplication.feature.deviceadmin.presentation.viewmodel.DeviceAdminViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class DeviceAdminActivity : ComponentActivity() {
    private val deviceAdminViewModel: DeviceAdminViewModel by viewModel()

    private val requestDeviceAdmin = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            navigateToMainActivity()
        } else {
            // Handle the case where the user denied the request
            deviceAdminViewModel.checkDeviceAdminStatus()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DeviceAdminScreen(
                deviceAdminViewModel,
                onRequestDeviceAdmin = { requestDeviceAdmin.launch(deviceAdminViewModel.requestDeviceAdmin()) },
                onDeviceAdminActivated = { navigateToMainActivity() }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        deviceAdminViewModel.checkDeviceAdminStatus()
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}