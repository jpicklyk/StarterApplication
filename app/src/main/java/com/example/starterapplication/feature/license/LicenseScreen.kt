package com.example.starterapplication.feature.license

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.jpicklyk.knox.licensing.domain.LicenseStartupResult

@Composable
fun LicenseScreen(
    viewModel: LicenseViewModel = hiltViewModel()
) {
    val licenseStatus by viewModel.licenseStatus.collectAsState()
    val isDeviceAdmin by viewModel.isDeviceAdmin.collectAsState()
    val hasConfiguredKey = viewModel.hasConfiguredLicenseKey
    val configuredKey = viewModel.configuredLicenseKey
    var useCustomKey by remember { mutableStateOf(false) }
    var customLicenseKey by remember { mutableStateOf("") }

    val deviceAdminLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.refreshDeviceAdminStatus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Knox License Management",
            style = MaterialTheme.typography.headlineMedium
        )

        // Device Admin Status Card
        DeviceAdminCard(
            isDeviceAdmin = isDeviceAdmin,
            onRequestAdmin = { deviceAdminLauncher.launch(viewModel.getDeviceAdminIntent()) }
        )

        LicenseStatusCard(licenseStatus)

        Spacer(modifier = Modifier.height(8.dp))

        // Show configured key status
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (hasConfiguredKey)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (hasConfiguredKey) "License Key Configured" else "No License Key Configured",
                    style = MaterialTheme.typography.titleSmall
                )
                if (hasConfiguredKey) {
                    Text(
                        text = "Key: ${configuredKey.take(10)}...${configuredKey.takeLast(6)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    Text(
                        text = "Add knox.license to local.properties",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // Activate with configured key button
        if (hasConfiguredKey && !useCustomKey) {
            Button(
                onClick = { viewModel.initializeLicense() },
                modifier = Modifier.fillMaxWidth(),
                enabled = isDeviceAdmin &&
                         licenseStatus !is LicenseStartupResult.AlreadyActivated &&
                         licenseStatus !is LicenseStartupResult.ActivatedNow
            ) {
                Text(
                    text = when {
                        !isDeviceAdmin -> "Enable Device Admin First"
                        licenseStatus is LicenseStartupResult.AlreadyActivated -> "Already Activated"
                        licenseStatus is LicenseStartupResult.ActivatedNow -> "Activated"
                        else -> "Activate with Configured Key"
                    }
                )
            }
        }

        // Option to use custom key
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = useCustomKey,
                onCheckedChange = { useCustomKey = it }
            )
            Text(
                text = "Use custom license key",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (useCustomKey) {
            OutlinedTextField(
                value = customLicenseKey,
                onValueChange = { customLicenseKey = it },
                label = { Text("Custom License Key") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Enter Knox License Key") }
            )

            Button(
                onClick = { viewModel.initializeLicenseWithKey(customLicenseKey) },
                modifier = Modifier.fillMaxWidth(),
                enabled = isDeviceAdmin &&
                         customLicenseKey.isNotBlank() &&
                         licenseStatus !is LicenseStartupResult.AlreadyActivated
            ) {
                Text(text = if (isDeviceAdmin) "Activate with Custom Key" else "Enable Device Admin First")
            }
        }
    }
}

@Composable
private fun DeviceAdminCard(
    isDeviceAdmin: Boolean,
    onRequestAdmin: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isDeviceAdmin)
                Color(0xFF4CAF50).copy(alpha = 0.1f)
            else
                Color(0xFFFF9800).copy(alpha = 0.1f)
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
                imageVector = if (isDeviceAdmin) Icons.Default.CheckCircle else Icons.Default.AdminPanelSettings,
                contentDescription = null,
                tint = if (isDeviceAdmin) Color(0xFF4CAF50) else Color(0xFFFF9800),
                modifier = Modifier.size(40.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isDeviceAdmin) "Device Admin Active" else "Device Admin Required",
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isDeviceAdmin) Color(0xFF4CAF50) else Color(0xFFFF9800)
                )
                Text(
                    text = if (isDeviceAdmin)
                        "Knox license activation is available."
                    else
                        "Knox requires Device Administrator privileges.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (!isDeviceAdmin) {
                OutlinedButton(onClick = onRequestAdmin) {
                    Text("Enable")
                }
            }
        }
    }
}

@Composable
private fun LicenseStatusCard(status: LicenseStartupResult) {
    val (icon, iconColor, statusText, description) = when (status) {
        is LicenseStartupResult.NotChecked -> StatusInfo(
            icon = Icons.Default.HourglassEmpty,
            color = Color.Gray,
            title = "Not Checked",
            description = "License status has not been checked yet."
        )
        is LicenseStartupResult.AlreadyActivated -> StatusInfo(
            icon = Icons.Default.CheckCircle,
            color = Color(0xFF4CAF50),
            title = "Activated",
            description = "Knox license is already activated and ready."
        )
        is LicenseStartupResult.ActivatedNow -> StatusInfo(
            icon = Icons.Default.CheckCircle,
            color = Color(0xFF4CAF50),
            title = "Just Activated",
            description = "Knox license was activated successfully."
        )
        is LicenseStartupResult.ActivationFailed -> StatusInfo(
            icon = Icons.Default.Error,
            color = Color(0xFFF44336),
            title = "Activation Failed",
            description = status.reason
        )
        is LicenseStartupResult.InitializationError -> StatusInfo(
            icon = Icons.Default.Warning,
            color = Color(0xFFFF9800),
            title = "Initialization Error",
            description = status.reason
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = iconColor.copy(alpha = 0.1f)
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
                tint = iconColor,
                modifier = Modifier.size(48.dp)
            )
            Column {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.titleMedium,
                    color = iconColor
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

private data class StatusInfo(
    val icon: ImageVector,
    val color: Color,
    val title: String,
    val description: String
)
