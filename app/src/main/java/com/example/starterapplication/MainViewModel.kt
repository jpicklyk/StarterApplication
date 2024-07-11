package com.example.starterapplication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.starterapplication.core.knox.api.ApiResult
import com.example.starterapplication.core.knox.feature.domain.KnoxFeatureManager
import com.example.starterapplication.core.knox.feature.domain.model.KnoxFeature
import com.example.starterapplication.core.knox.feature.domain.model.KnoxFeatureKey
import com.example.starterapplication.core.knox.feature.presentation.KnoxFeatureState
import com.example.starterapplication.knox_standard.license.domain.repository.LicenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import javax.inject.Inject

data class KnoxFeaturesState(
    val isLoading: Boolean = false,
    val features: List<KnoxFeature<*>> = emptyList(),
    val error: String? = null
)

@KoinViewModel
class MainViewModel @Inject constructor(
    private val licenseRepository: LicenseRepository,
    private val knoxFeatureManager: KnoxFeatureManager
) : ViewModel() {

    val licenseState = licenseRepository.licenseState
    private val _uiState = MutableStateFlow(KnoxFeaturesState())
    val uiState: StateFlow<KnoxFeaturesState> = _uiState.asStateFlow()

    init {
        loadFeatures()
    }

    private fun loadFeatures() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = knoxFeatureManager.getAllFeatures()) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        features = result.data
                    )
                }

                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.apiError?.message
                    )
                }

                is ApiResult.NotSupported -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Feature not supported"
                    )
                }
            }
            println("isLoading: ${_uiState.value.isLoading}, features: ${_uiState.value.features}, error: ${_uiState.value.error}")
        }
    }

    fun toggleFeatureState(feature: KnoxFeature<*>) {
        viewModelScope.launch {
            val newState = KnoxFeatureState(!feature.state.enabled, feature.state.value)
            when (val result = knoxFeatureManager.setFeatureState(
                feature.key as KnoxFeatureKey<*>,
                newState as KnoxFeatureState<*>
            )) {
                is ApiResult.Success -> {
                    loadFeatures() // Reload all features to reflect the change
                }

                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to update feature state: ${result.apiError?.message}"
                    )
                }

                is ApiResult.NotSupported -> {
                    _uiState.value = _uiState.value.copy(
                        error = "Toggling feature state is not supported"
                    )
                }
            }
        }
    }

    fun activateLicense() {
        viewModelScope.launch {
            licenseRepository.activateLicense()
        }
    }

    fun deactivateLicense() {
        viewModelScope.launch {
            licenseRepository.deactivateLicense()
        }
    }

    fun refreshLicenseInfo() {
        viewModelScope.launch {
            licenseRepository.refreshLicenseState()
        }
    }
}