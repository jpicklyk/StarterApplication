package com.example.starterapplication.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.jpicklyk.knox.licensing.domain.KnoxLicenseInitializer
import com.github.jpicklyk.knox.licensing.domain.LicenseStartupResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val licenseInitializer: KnoxLicenseInitializer
) : ViewModel() {

    val licenseStatus: StateFlow<LicenseStartupResult> = licenseInitializer.licenseStatus
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LicenseStartupResult.NotChecked
        )

    val isReady: Boolean
        get() = licenseInitializer.isReady
}
