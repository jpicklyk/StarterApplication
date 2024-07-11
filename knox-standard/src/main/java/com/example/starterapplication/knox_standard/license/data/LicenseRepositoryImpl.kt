package com.example.starterapplication.knox_standard.license.data

import android.util.Log
import com.example.starterapplication.knox_standard.license.domain.repository.LicenseRepository
import com.example.starterapplication.knox_standard.license.presentation.LicenseState
import com.example.starterapplication.knox_standard.license.domain.usecase.GetLicenseInfoUseCase
import com.example.starterapplication.knox_standard.license.domain.usecase.KnoxLicenseUseCase
import dagger.Component.Factory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single

@Single
internal class LicenseRepositoryImpl (
    private val knoxLicenseUseCase: KnoxLicenseUseCase,
    private val getLicenseInfoUseCase: GetLicenseInfoUseCase
) : LicenseRepository {
    private val tag = "KnoxLicenseRepository"

    private val _licenseState = MutableStateFlow<LicenseState>(LicenseState.Loading)
    override val licenseState: StateFlow<LicenseState> = _licenseState.asStateFlow()

    init {
        // Launch a coroutine to fetch the initial license state
        CoroutineScope(Dispatchers.IO).launch {
            refreshLicenseState()
        }
    }

    override suspend fun refreshLicenseState() {
        Log.d(tag, "Refreshing license state")
        _licenseState.value = getLicenseInfoUseCase()
    }

    override suspend fun activateLicense() {
        Log.d(tag, "Activating license")
        _licenseState.value = knoxLicenseUseCase()
        refreshLicenseState() // Refresh the state after activation
    }

    override suspend fun deactivateLicense() {
        Log.d(tag, "Deactivating license")
        _licenseState.value = knoxLicenseUseCase(activate = false)
        refreshLicenseState() // Refresh the state after deactivation
    }
}