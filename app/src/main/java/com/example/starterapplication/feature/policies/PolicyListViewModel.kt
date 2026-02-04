package com.example.starterapplication.feature.policies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.jpicklyk.knox.licensing.domain.KnoxLicenseInitializer
import com.github.jpicklyk.knox.licensing.domain.LicenseStartupResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.sfelabs.knox.core.feature.api.PolicyGroupingStrategy
import net.sfelabs.knox.core.feature.api.ResolvedPolicyGroup
import net.sfelabs.knox.core.feature.domain.registry.PolicyRegistry
import javax.inject.Inject

data class PolicyListUiState(
    val isLoading: Boolean = true,
    val resolvedGroups: List<ResolvedPolicyGroup> = emptyList(),
    val searchQuery: String = "",
    val error: String? = null
)

@HiltViewModel
class PolicyListViewModel @Inject constructor(
    private val policyRegistry: PolicyRegistry,
    private val groupingStrategy: PolicyGroupingStrategy,
    private val licenseInitializer: KnoxLicenseInitializer
) : ViewModel() {

    private val _uiState = MutableStateFlow(PolicyListUiState())
    val uiState: StateFlow<PolicyListUiState> = _uiState.asStateFlow()

    val isLicenseActivated: StateFlow<Boolean> = licenseInitializer.licenseStatus
        .map { status ->
            status is LicenseStartupResult.AlreadyActivated ||
            status is LicenseStartupResult.ActivatedNow
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    init {
        loadPolicies()
    }

    fun loadPolicies() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val resolvedGroups = groupingStrategy.resolveAllGroups(policyRegistry)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    resolvedGroups = resolvedGroups
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load policies"
                )
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun getFilteredGroups(): List<ResolvedPolicyGroup> {
        val query = _uiState.value.searchQuery.lowercase()
        if (query.isBlank()) return _uiState.value.resolvedGroups

        return _uiState.value.resolvedGroups.mapNotNull { group ->
            val filteredPolicies = group.policies.filter { policy ->
                policy.title.lowercase().contains(query) ||
                policy.description.lowercase().contains(query)
            }
            if (filteredPolicies.isNotEmpty()) {
                group.copy(policies = filteredPolicies)
            } else {
                null
            }
        }
    }
}
