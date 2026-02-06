package com.example.starterapplication.feature.license

import android.app.Application
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.starterapplication.feature.license.BuildConfig
import com.example.starterapplication.feature.license.admin.StarterDeviceAdminReceiver
import com.github.jpicklyk.knox.licensing.domain.KnoxLicenseInitializer
import com.github.jpicklyk.knox.licensing.domain.LicenseStartupResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LicenseViewModel @Inject constructor(
    private val application: Application,
    private val licenseInitializer: KnoxLicenseInitializer
) : ViewModel() {

    private val devicePolicyManager: DevicePolicyManager =
        application.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

    private val adminComponentName: ComponentName =
        ComponentName(application, StarterDeviceAdminReceiver::class.java)

    private val _isDeviceAdmin = MutableStateFlow(checkDeviceAdminStatus())
    val isDeviceAdmin: StateFlow<Boolean> = _isDeviceAdmin.asStateFlow()

    val licenseStatus: StateFlow<LicenseStartupResult> = licenseInitializer.licenseStatus
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LicenseStartupResult.NotChecked
        )

    val isReady: Boolean
        get() = licenseInitializer.isReady

    val isInitialized: Boolean
        get() = licenseInitializer.isInitialized

    init {
        // Auto-initialize license on startup if device admin is active and key is configured
        if (checkDeviceAdminStatus() && hasConfiguredLicenseKey) {
            initializeLicense()
        }
    }

    private fun checkDeviceAdminStatus(): Boolean {
        return devicePolicyManager.isAdminActive(adminComponentName)
    }

    fun refreshDeviceAdminStatus() {
        _isDeviceAdmin.value = checkDeviceAdminStatus()
    }

    fun getDeviceAdminIntent(): Intent {
        return Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponentName)
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Device Administrator is required for Knox license activation."
            )
        }
    }

    /**
     * Returns the configured license key from BuildConfig.
     * This is read from local.properties via the knox.license property.
     */
    val configuredLicenseKey: String
        get() = BuildConfig.KNOX_LICENSE_KEY

    /**
     * Returns true if a valid license key is configured (not the default placeholder).
     */
    val hasConfiguredLicenseKey: Boolean
        get() = configuredLicenseKey != "KNOX_LICENSE_KEY_NOT_FOUND" && configuredLicenseKey.isNotBlank()

    /**
     * Initialize the Knox license using the configured key from BuildConfig.
     */
    fun initializeLicense() {
        viewModelScope.launch {
            licenseInitializer.initialize(
                context = application,
                defaultKey = configuredLicenseKey
            )
        }
    }

    /**
     * Initialize the Knox license with a custom key (for testing/override).
     */
    fun initializeLicenseWithKey(licenseKey: String) {
        viewModelScope.launch {
            licenseInitializer.initialize(
                context = application,
                defaultKey = licenseKey
            )
        }
    }
}
