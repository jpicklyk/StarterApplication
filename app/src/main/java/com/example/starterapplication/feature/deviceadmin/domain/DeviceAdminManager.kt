package com.example.starterapplication.feature.deviceadmin.domain

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.starterapplication.feature.deviceadmin.receivers.AdminReceiver

class DeviceAdminManager(context: Context) {
    private val tag = "DeviceAdminManager"
    private val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val componentName = ComponentName(context, AdminReceiver::class.java)

    init {
        Log.d(tag, "DeviceAdminManager initialized with componentName: $componentName")
    }


    fun isDeviceAdminActive(): Boolean {
        return devicePolicyManager.isAdminActive(componentName)
    }

    fun getDeviceAdminRequestIntent(): Intent {
        return Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
            putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Device admin permission is required for security features.")
        }
    }
}