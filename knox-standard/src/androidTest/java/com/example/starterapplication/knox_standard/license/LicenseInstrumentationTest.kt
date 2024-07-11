package com.example.starterapplication.knox_standard.license

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.starterapplication.knox_standard.license.domain.repository.LicenseRepository
import com.example.starterapplication.knox_standard.license.presentation.LicenseState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@RunWith(AndroidJUnit4::class)
class LicenseInstrumentationTest : KoinComponent {

    private val licenseRepository: LicenseRepository by inject()

    @Before
    fun setup() {
        // Ensure we start from a deactivated state
        runBlocking {
            if (licenseRepository.licenseState.first() is LicenseState.Activated) {
                licenseRepository.deactivateLicense()
            }
        }
    }

    @Test
    fun testLicenseActivation() = runBlocking {
        // Activate the license
        licenseRepository.activateLicense()

        // Check if the license is activated
        val state = licenseRepository.licenseState.first()
        assertTrue("License should be activated", state is LicenseState.Activated)
    }

    @Test
    fun testLicenseStatus() = runBlocking {
        // Check initial status (should be deactivated from setup)
        var state = licenseRepository.licenseState.first()
        assertTrue("Initial license state should be deactivated", state is LicenseState.NotActivated)

        // Activate the license
        licenseRepository.activateLicense()

        // Refresh and check status
        licenseRepository.refreshLicenseState()
        state = licenseRepository.licenseState.first()
        assertTrue("License should be activated after activation", state is LicenseState.Activated)
    }

    @Test
    fun testLicenseDeactivation() = runBlocking {
        // First, activate the license
        licenseRepository.activateLicense()

        // Then deactivate it
        licenseRepository.deactivateLicense()

        // Check if the license is deactivated
        val state = licenseRepository.licenseState.first()
        assertTrue("License should be deactivated", state is LicenseState.NotActivated)
    }
}