package com.example.starterapplication.feature.policies

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Slider
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import net.sfelabs.knox.core.domain.usecase.model.ApiResult
import net.sfelabs.knox.core.feature.api.PolicyCategory
import net.sfelabs.knox.core.feature.api.PolicyComponent
import net.sfelabs.knox.core.feature.api.PolicyState
import net.sfelabs.knox.core.feature.api.PolicyUiConverter
import net.sfelabs.knox.core.feature.api.ResolvedPolicyGroup
import net.sfelabs.knox.core.feature.ui.model.ConfigurationOption

@Composable
fun PolicyListScreen(
    onPolicyClick: (String) -> Unit = {},
    onNavigateToLicense: () -> Unit = {},
    viewModel: PolicyListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isLicenseActivated by viewModel.isLicenseActivated.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Knox Policies",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (!isLicenseActivated) {
            LicenseRequiredCard(onNavigateToLicense = onNavigateToLicense)
        } else {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                label = { Text("Search policies") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true
            )

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.error ?: "Unknown error",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                else -> {
                    val filteredGroups = viewModel.getFilteredGroups()
                    if (filteredGroups.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (uiState.searchQuery.isNotBlank())
                                    "No policies match your search"
                                else
                                    "No policies registered",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    } else {
                        PolicyGroupedList(
                            resolvedGroups = filteredGroups,
                            onPolicyClick = onPolicyClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LicenseRequiredCard(
    onNavigateToLicense: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "License Required",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.size(16.dp))
                Text(
                    text = "License Required",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Knox license must be activated to view and manage policies.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.size(24.dp))
                Button(onClick = onNavigateToLicense) {
                    Text("Go to License")
                }
            }
        }
    }
}

@Composable
private fun PolicyGroupedList(
    resolvedGroups: List<ResolvedPolicyGroup>,
    onPolicyClick: (String) -> Unit
) {
    // Track expanded state for each group
    val expandedGroups = remember { mutableStateMapOf<String, Boolean>() }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        resolvedGroups.forEach { resolvedGroup ->
            val isExpanded = expandedGroups[resolvedGroup.group.id] ?: false

            item(key = "header_${resolvedGroup.group.id}") {
                CollapsibleGroupHeader(
                    groupName = resolvedGroup.group.displayName,
                    policyCount = resolvedGroup.policies.size,
                    isExpanded = isExpanded,
                    onToggle = {
                        expandedGroups[resolvedGroup.group.id] = !isExpanded
                    }
                )
            }

            if (isExpanded) {
                items(
                    items = resolvedGroup.policies,
                    key = { "${resolvedGroup.group.id}_${it.policyName}" }
                ) { policy ->
                    InteractivePolicyCard(
                        policy = policy,
                        onClick = { onPolicyClick(policy.policyName) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CollapsibleGroupHeader(
    groupName: String,
    policyCount: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "rotation"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = groupName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "$policyCount ${if (policyCount == 1) "policy" else "policies"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .size(24.dp)
                    .rotate(rotationAngle)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun InteractivePolicyCard(
    policy: PolicyComponent<out PolicyState>,
    onClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var policyState by remember { mutableStateOf<PolicyState?>(null) }
    var configOptions by remember { mutableStateOf<List<ConfigurationOption>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isUpdating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var showConfig by remember { mutableStateOf(false) }

    // Load initial state and configuration options
    LaunchedEffect(policy.policyName) {
        isLoading = true
        errorMessage = null
        try {
            val state = policy.handler.getState()
            policyState = state
            // Get configuration options from uiConverter
            @Suppress("UNCHECKED_CAST")
            val converter = policy.uiConverter as? PolicyUiConverter<PolicyState>
            configOptions = converter?.getConfigurationOptions(state) ?: emptyList()
        } catch (e: Exception) {
            errorMessage = e.message ?: "Failed to load state"
        }
        isLoading = false
    }

    // Function to update policy state
    fun updatePolicy(newEnabled: Boolean, options: List<ConfigurationOption>) {
        scope.launch {
            isUpdating = true
            errorMessage = null
            successMessage = null
            try {
                @Suppress("UNCHECKED_CAST")
                val converter = policy.uiConverter as? PolicyUiConverter<PolicyState>
                val newState = converter?.fromUiState(newEnabled, options)
                    ?: policyState?.withEnabled(newEnabled)

                if (newState != null) {
                    @Suppress("UNCHECKED_CAST")
                    val handler = policy.handler as? net.sfelabs.knox.core.feature.domain.usecase.handler.PolicyHandler<PolicyState>
                    if (handler != null) {
                        when (val result = handler.setState(newState)) {
                            is ApiResult.Success -> {
                                policyState = newState
                                configOptions = converter?.getConfigurationOptions(newState) ?: emptyList()
                                successMessage = "Updated"
                            }
                            is ApiResult.Error -> {
                                errorMessage = result.apiError?.message ?: "Update failed"
                            }
                            is ApiResult.NotSupported -> {
                                errorMessage = "Not supported"
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Update failed"
            }
            isUpdating = false
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                errorMessage != null -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                policyState?.isSupported == false -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = policy.title,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = policy.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Show config button if there are configuration options
                if (configOptions.isNotEmpty() && policyState?.isSupported != false && !isLoading) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Configure",
                        tint = if (showConfig) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { showConfig = !showConfig }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                // Show appropriate control based on policy category and state
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    policyState?.isSupported == false -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Not supported",
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "N/A",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                    policy.category == PolicyCategory.Toggle ||
                    policy.category == PolicyCategory.ConfigurableToggle -> {
                        val currentState = policyState
                        if (currentState != null) {
                            if (isUpdating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Switch(
                                    checked = currentState.isEnabled,
                                    onCheckedChange = { newEnabled ->
                                        updatePolicy(newEnabled, configOptions)
                                    },
                                    enabled = !isUpdating
                                )
                            }
                        }
                    }
                    else -> {
                        Text(
                            text = policy.category.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // Show configuration options when expanded
            AnimatedVisibility(visible = showConfig && configOptions.isNotEmpty()) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider()
                    Text(
                        text = "Configuration",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    configOptions.forEachIndexed { index, option ->
                        when (option) {
                            is ConfigurationOption.Toggle -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = option.label,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Switch(
                                        checked = option.isEnabled,
                                        onCheckedChange = { newValue ->
                                            val updatedOptions = configOptions.toMutableList()
                                            updatedOptions[index] = option.copy(isEnabled = newValue)
                                            configOptions = updatedOptions
                                            updatePolicy(policyState?.isEnabled ?: false, updatedOptions)
                                        },
                                        enabled = !isUpdating
                                    )
                                }
                            }

                            is ConfigurationOption.Choice -> {
                                var expanded by remember { mutableStateOf(false) }
                                Column {
                                    Text(
                                        text = option.label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                    ExposedDropdownMenuBox(
                                        expanded = expanded,
                                        onExpandedChange = { expanded = !expanded }
                                    ) {
                                        OutlinedTextField(
                                            value = option.selected,
                                            onValueChange = {},
                                            readOnly = true,
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                            textStyle = MaterialTheme.typography.bodyMedium
                                        )
                                        ExposedDropdownMenu(
                                            expanded = expanded,
                                            onDismissRequest = { expanded = false }
                                        ) {
                                            option.options.forEach { selectionOption ->
                                                DropdownMenuItem(
                                                    text = { Text(selectionOption) },
                                                    onClick = {
                                                        expanded = false
                                                        val updatedOptions = configOptions.toMutableList()
                                                        updatedOptions[index] = option.copy(selected = selectionOption)
                                                        configOptions = updatedOptions
                                                        updatePolicy(policyState?.isEnabled ?: false, updatedOptions)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            is ConfigurationOption.NumberInput -> {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = option.label,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = option.value.toString(),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    val range = option.range
                                    if (range != null) {
                                        Slider(
                                            value = option.value.toFloat(),
                                            onValueChange = { newValue ->
                                                val updatedOptions = configOptions.toMutableList()
                                                updatedOptions[index] = option.copy(value = newValue.toInt())
                                                configOptions = updatedOptions
                                            },
                                            onValueChangeFinished = {
                                                updatePolicy(policyState?.isEnabled ?: false, configOptions)
                                            },
                                            valueRange = range.first.toFloat()..range.last.toFloat(),
                                            steps = (range.last - range.first - 1).coerceAtLeast(0),
                                            enabled = !isUpdating
                                        )
                                    } else {
                                        var textValue by remember(option.value) { mutableStateOf(option.value.toString()) }
                                        OutlinedTextField(
                                            value = textValue,
                                            onValueChange = { newText ->
                                                textValue = newText.filter { it.isDigit() || it == '-' }
                                                textValue.toIntOrNull()?.let { newValue ->
                                                    val updatedOptions = configOptions.toMutableList()
                                                    updatedOptions[index] = option.copy(value = newValue)
                                                    configOptions = updatedOptions
                                                }
                                            },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.fillMaxWidth(),
                                            textStyle = MaterialTheme.typography.bodyMedium,
                                            singleLine = true
                                        )
                                        Button(
                                            onClick = { updatePolicy(policyState?.isEnabled ?: false, configOptions) },
                                            modifier = Modifier.padding(top = 8.dp),
                                            enabled = !isUpdating
                                        ) {
                                            Text("Apply")
                                        }
                                    }
                                }
                            }

                            is ConfigurationOption.TextInput -> {
                                Column {
                                    Text(
                                        text = option.label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                    var textValue by remember(option.value) { mutableStateOf(option.value) }
                                    OutlinedTextField(
                                        value = textValue,
                                        onValueChange = { newText ->
                                            val maxLen = option.maxLength
                                            textValue = if (maxLen != null) newText.take(maxLen) else newText
                                            val updatedOptions = configOptions.toMutableList()
                                            updatedOptions[index] = option.copy(value = textValue)
                                            configOptions = updatedOptions
                                        },
                                        placeholder = option.hint?.let { { Text(it) } },
                                        modifier = Modifier.fillMaxWidth(),
                                        textStyle = MaterialTheme.typography.bodyMedium,
                                        singleLine = true,
                                        supportingText = option.maxLength?.let { maxLen ->
                                            { Text("${textValue.length}/$maxLen") }
                                        }
                                    )
                                    Button(
                                        onClick = { updatePolicy(policyState?.isEnabled ?: false, configOptions) },
                                        modifier = Modifier.padding(top = 8.dp),
                                        enabled = !isUpdating
                                    ) {
                                        Text("Apply")
                                    }
                                }
                            }

                            is ConfigurationOption.TextList -> {
                                Column {
                                    Text(
                                        text = option.label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )

                                    // Display existing items as chips
                                    if (option.values.isNotEmpty()) {
                                        FlowRow(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 8.dp),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            option.values.forEach { value ->
                                                InputChip(
                                                    selected = false,
                                                    onClick = { },
                                                    label = { Text(value) },
                                                    trailingIcon = {
                                                        Icon(
                                                            imageVector = Icons.Default.Close,
                                                            contentDescription = "Remove",
                                                            modifier = Modifier
                                                                .size(16.dp)
                                                                .clickable {
                                                                    val newValues = option.values - value
                                                                    val updatedOptions = configOptions.toMutableList()
                                                                    updatedOptions[index] = option.copy(values = newValues)
                                                                    configOptions = updatedOptions
                                                                    updatePolicy(policyState?.isEnabled ?: false, updatedOptions)
                                                                }
                                                        )
                                                    },
                                                    colors = InputChipDefaults.inputChipColors(
                                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                                    )
                                                )
                                            }
                                        }
                                    }

                                    // Input for adding new items
                                    var newItemText by remember { mutableStateOf("") }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = newItemText,
                                            onValueChange = { newItemText = it },
                                            placeholder = option.hint?.let { { Text(it) } },
                                            modifier = Modifier.weight(1f),
                                            textStyle = MaterialTheme.typography.bodyMedium,
                                            singleLine = true
                                        )
                                        IconButton(
                                            onClick = {
                                                if (newItemText.isNotBlank()) {
                                                    val newValues = option.values + newItemText.trim()
                                                    val updatedOptions = configOptions.toMutableList()
                                                    updatedOptions[index] = option.copy(values = newValues)
                                                    configOptions = updatedOptions
                                                    newItemText = ""
                                                    updatePolicy(policyState?.isEnabled ?: false, updatedOptions)
                                                }
                                            },
                                            enabled = !isUpdating && newItemText.isNotBlank()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Add",
                                                tint = if (newItemText.isNotBlank())
                                                    MaterialTheme.colorScheme.primary
                                                else
                                                    MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Show status messages
            AnimatedVisibility(visible = errorMessage != null || successMessage != null) {
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (errorMessage != null) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else if (successMessage != null) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = successMessage ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }

            // Show capabilities
            Text(
                text = policy.capabilities.joinToString(", ") { it.name },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
