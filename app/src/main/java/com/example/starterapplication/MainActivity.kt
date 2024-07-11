package com.example.starterapplication

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.example.starterapplication.core.knox.feature.domain.model.KnoxFeature
import com.example.starterapplication.feature.deviceadmin.domain.DeviceAdminManager
import com.example.starterapplication.feature.deviceadmin.presentation.ui.DeviceAdminActivity
import com.example.starterapplication.ui.theme.StarterApplicationTheme
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private val deviceAdminManager: DeviceAdminManager by inject()
    private val viewModel: MainViewModel by viewModel()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        var keepSplashScreenOn = true
        splashScreen.setKeepOnScreenCondition { keepSplashScreenOn }

        lifecycleScope.launch {
            if (!deviceAdminManager.isDeviceAdminActive()) {
                keepSplashScreenOn = false
                startActivity(Intent(this@MainActivity, DeviceAdminActivity::class.java))
                finish()
                return@launch
            }

            keepSplashScreenOn = false

            enableEdgeToEdge()
            setContent {
                StarterApplicationTheme {
                    val licenseState by viewModel.licenseState.collectAsState()
                    val uiState by viewModel.uiState.collectAsState()

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = { Text("Knox Feature Manager") },
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    ) { innerPadding ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            LicenseSection(
                                licenseState = licenseState,
                                onActivate = viewModel::activateLicense,
                                onDeactivate = viewModel::deactivateLicense,
                                onRefresh = viewModel::refreshLicenseInfo
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            FeatureList(
                                uiState = uiState,
                                onToggleFeature = viewModel::toggleFeatureState
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LicenseSection(
    licenseState: Any,
    onActivate: () -> Unit,
    onDeactivate: () -> Unit,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "License State: $licenseState",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = onActivate) {
                Text("Activate License")
            }
            Button(onClick = onDeactivate) {
                Text("Deactivate License")
            }

        }
        Row {
            Button(onClick = onRefresh) {
                Text("Refresh License Info")
            }
        }
    }
}

@Composable
fun FeatureList(
    uiState: KnoxFeaturesState,
    onToggleFeature: (KnoxFeature<*>) -> Unit
) {
    when {
        uiState.isLoading -> {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
        uiState.error != null -> {
            Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "Error: ${uiState.error}",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
        else -> {
            Column {
                Text(
                    text = "Feature List",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(uiState.features) { feature ->
                        KnoxFeatureItem(
                            feature = feature,
                            onToggle = { onToggleFeature(feature) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun KnoxFeatureItem(
    feature: KnoxFeature<*>,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = feature.key.featureName,
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Value: ${feature.state.value}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Switch(
                checked = feature.state.enabled,
                onCheckedChange = { onToggle() }
            )
        }
    }
}