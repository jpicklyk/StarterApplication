package com.example.starterapplication.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.starterapplication.feature.license.LicenseViewModel
import com.github.jpicklyk.knox.licensing.domain.LicenseStartupResult

@Composable
fun HomeScreen(
    onNavigateToLicense: () -> Unit,
    onNavigateToPolicies: () -> Unit,
    licenseViewModel: LicenseViewModel = hiltViewModel()
) {
    val licenseStatus by licenseViewModel.licenseStatus.collectAsState()
    val isLicenseReady = licenseViewModel.isReady

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Knox Starter Application",
            style = MaterialTheme.typography.headlineLarge
        )

        Text(
            text = "Demonstrates Knox SDK integration patterns with Hilt DI and Jetpack Compose.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // License Status Summary
        LicenseStatusSummary(
            licenseStatus = licenseStatus,
            isReady = isLicenseReady
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Navigation Cards
        NavigationCard(
            title = "License Management",
            description = "Activate and manage Knox license",
            icon = Icons.Default.Key,
            onClick = onNavigateToLicense
        )

        NavigationCard(
            title = "Policy Explorer",
            description = "Browse and modify Knox policies",
            icon = Icons.Default.Policy,
            onClick = onNavigateToPolicies,
            enabled = isLicenseReady
        )
    }
}

@Composable
private fun LicenseStatusSummary(
    licenseStatus: LicenseStartupResult,
    isReady: Boolean
) {
    val (icon, color, text) = when {
        isReady -> Triple(
            Icons.Default.CheckCircle,
            Color(0xFF4CAF50),
            "License Active"
        )
        licenseStatus is LicenseStartupResult.ActivationFailed ||
        licenseStatus is LicenseStartupResult.InitializationError -> Triple(
            Icons.Default.Warning,
            Color(0xFFF44336),
            "License Error"
        )
        else -> Triple(
            Icons.Default.Warning,
            Color(0xFFFF9800),
            "License Not Active"
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = color
        )
    }
}

@Composable
private fun NavigationCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        colors = CardDefaults.cardColors(
            containerColor = if (enabled)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(40.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (enabled)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Text(
                    text = if (enabled) description else "Activate license first",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (enabled)
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}
