package com.expensetracker.app.ui

import android.os.Debug
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.expensetracker.app.data.llm.LlmModelState
import kotlinx.coroutines.delay

@Composable
fun SettingsRoute(viewModel: SettingsViewModel = hiltViewModel()) {
    val modelState by viewModel.modelState.collectAsStateWithLifecycle()
    val aiRunning by viewModel.aiRunning.collectAsStateWithLifecycle()
    var showAi by rememberSaveable { mutableStateOf(false) }
    var confirmClear by rememberSaveable { mutableStateOf(false) }

    if (showAi) {
        AiStatusScreen(
            modelState = modelState,
            aiRunning = aiRunning,
            onBack = { showAi = false }
        )
    } else {
        SettingsScreen(
            aiSummary = aiSummary(modelState, aiRunning),
            onAiClick = { showAi = true },
            onClearClick = { confirmClear = true }
        )
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Clear all data?") },
            text = {
                Text("This deletes every transaction and autopay on this device. It cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAll()
                        confirmClear = false
                    }
                ) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsScreen(
    aiSummary: String,
    onAiClick: () -> Unit,
    onClearClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "AI",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        item {
            SettingCard(
                title = "On-device AI",
                subtitle = aiSummary,
                onClick = onAiClick
            )
        }
        item {
            Text(
                text = "Data",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
        item {
            SettingCard(
                title = "Clear all data",
                subtitle = "Delete all transactions and autopays",
                danger = true,
                onClick = onClearClick
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingCard(
    title: String,
    subtitle: String,
    danger: Boolean = false,
    onClick: () -> Unit
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (danger) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null)
        }
    }
}

@Composable
private fun AiStatusScreen(
    modelState: LlmModelState,
    aiRunning: Boolean,
    onBack: () -> Unit
) {
    var ramBytes by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            ramBytes = processRamBytes()
            delay(RAM_REFRESH_MS)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onBack) {
            Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = null)
            Text("Settings")
        }
        Text(
            text = "On-device AI",
            style = MaterialTheme.typography.headlineSmall
        )
        Card(Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatusRow("Status", aiStatusLabel(modelState, aiRunning))
                StatusRow("Model", modelLabel(modelState))
                StatusRow("RAM usage", formatBytes(ramBytes))
            }
        }
        Text(
            text = "RAM is the app process total, so it includes the loaded model while the AI is running. Updated every ${RAM_REFRESH_MS / 1000} seconds.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatusRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun aiSummary(state: LlmModelState, running: Boolean): String = when (state) {
    LlmModelState.Missing -> "Model not downloaded"
    is LlmModelState.Downloading -> "Downloading"
    is LlmModelState.Failed -> "Download failed"
    LlmModelState.Ready -> if (running) "Running" else "Ready, idle"
}

private fun aiStatusLabel(state: LlmModelState, running: Boolean): String = when (state) {
    LlmModelState.Missing -> "Not available"
    is LlmModelState.Downloading -> "Downloading"
    is LlmModelState.Failed -> "Failed"
    LlmModelState.Ready -> if (running) "Running" else "Idle"
}

private fun modelLabel(state: LlmModelState): String = when (state) {
    LlmModelState.Missing -> "Not downloaded"
    is LlmModelState.Downloading -> "${formatBytes(state.downloaded)} of ${
        if (state.total > 0) formatBytes(state.total) else "?"
    }"
    is LlmModelState.Failed -> state.message
    LlmModelState.Ready -> "Ready"
}

private fun processRamBytes(): Long {
    val info = Debug.MemoryInfo()
    Debug.getMemoryInfo(info)
    return info.totalPss.toLong() * 1024
}

private const val RAM_REFRESH_MS = 2_000L
