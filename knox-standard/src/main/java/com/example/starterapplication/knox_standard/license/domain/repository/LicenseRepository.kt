package com.example.starterapplication.knox_standard.license.domain.repository

import com.example.starterapplication.knox_standard.license.presentation.LicenseState
import kotlinx.coroutines.flow.StateFlow

interface LicenseRepository {
    val licenseState: StateFlow<LicenseState>
    suspend fun refreshLicenseState()
    suspend fun activateLicense()
    suspend fun deactivateLicense()
}