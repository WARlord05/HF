package com.expensetracker.app.ui

import android.content.Intent
import android.provider.Settings
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.expensetracker.app.data.llm.LlmModelManager
import com.expensetracker.app.data.llm.LlmModelState
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val llmViewModel: LlmViewModel = hiltViewModel()
    val llmState by llmViewModel.state.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var showLlmDialog by rememberSaveable { mutableStateOf(false) }
    var notificationAccess by remember { mutableStateOf(NotificationAccess.isGranted(context)) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                notificationAccess = NotificationAccess.isGranted(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text("Expense Tracker", fontWeight = FontWeight.SemiBold)
                    },
                    actions = {
                        TextButton(onClick = { showLlmDialog = true }) {
                            Text("AI")
                        }
                    }
                )
                PrivacyBanner()
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    icon = { Icon(Icons.Filled.List, contentDescription = null) },
                    label = { Text("Transactions") }
                )
                NavigationBarItem(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    icon = { Icon(Icons.Filled.Refresh, contentDescription = null) },
                    label = { Text("Autopays") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (!notificationAccess) {
                NotificationAccessCard(
                    onEnable = {
                        runCatching {
                            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                        }
                    }
                )
            }
            Box(Modifier.weight(1f)) {
                if (tab == 0) TransactionsRoute() else AutopaysRoute()
            }
        }
    }

    if (showLlmDialog) {
        LlmModelDialog(
            state = llmState,
            onDownload = llmViewModel::download,
            onDelete = llmViewModel::delete,
            onDismiss = { showLlmDialog = false }
        )
    }
}

@Composable
private fun PrivacyBanner() {
    Surface(color = MaterialTheme.colorScheme.secondaryContainer) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Parsing stays on this device. No servers, no bank logins. The optional AI model downloads once.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun NotificationAccessCard(onEnable: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, top = 10.dp, bottom = 10.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Notification access is off",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = "Enable it to track payments automatically.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            TextButton(onClick = onEnable) {
                Text("Enable")
            }
        }
    }
}

@Composable
private fun LlmModelDialog(
    state: LlmModelState,
    onDownload: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("On-device AI fallback") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Qwen2.5 0.5B can read unusual bank alerts that the normal parser misses. Amounts still come straight from the notification text.",
                    style = MaterialTheme.typography.bodySmall
                )
                when (state) {
                    LlmModelState.Missing -> Text(
                        text = "Model not downloaded (${LlmModelManager.MODEL_SIZE_LABEL}).",
                        style = MaterialTheme.typography.bodySmall
                    )

                    is LlmModelState.Downloading -> {
                        if (state.total > 0) {
                            LinearProgressIndicator(
                                progress = {
                                    (state.downloaded.toFloat() / state.total).coerceIn(0f, 1f)
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }
                        Text(
                            text = "Downloading ${formatBytes(state.downloaded)} of ${
                                if (state.total > 0) formatBytes(state.total) else "?"
                            }",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    LlmModelState.Ready -> Text(
                        text = "Ready. It unloads from memory after a minute of disuse.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    is LlmModelState.Failed -> Text(
                        text = "Download failed: ${state.message}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            when (state) {
                LlmModelState.Missing, is LlmModelState.Failed -> TextButton(onClick = onDownload) {
                    Text("Download")
                }

                LlmModelState.Ready -> TextButton(onClick = onDelete) {
                    Text("Remove")
                }

                is LlmModelState.Downloading -> TextButton(onClick = onDismiss) {
                    Text("Hide")
                }
            }
        },
        dismissButton = {
            if (state !is LlmModelState.Downloading) {
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    )
}

private fun formatBytes(bytes: Long): String = when {
    bytes >= 1_000_000_000 -> String.format(Locale.US, "%.1f GB", bytes / 1_000_000_000.0)
    bytes >= 1_000_000 -> String.format(Locale.US, "%.0f MB", bytes / 1_000_000.0)
    else -> String.format(Locale.US, "%.0f KB", bytes / 1_000.0)
}
