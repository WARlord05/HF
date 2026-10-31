package com.expensetracker.app.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.expensetracker.app.data.llm.LlmModelManager
import com.expensetracker.app.data.llm.LlmModelState
import com.expensetracker.app.ui.components.HairlineDivider
import com.expensetracker.app.ui.theme.LocalLedgerExtras
import com.expensetracker.app.ui.theme.ScreenTitleStyle
import java.util.Locale

@Composable
fun MainScreen(
    llmViewModel: LlmViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val llmState by llmViewModel.state.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var notificationAccess by remember { mutableStateOf(NotificationAccess.isGranted(context)) }
    var showAddEntry by remember { mutableStateOf(false) }
    var showLlmDialog by rememberSaveable { mutableStateOf(false) }
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
            PocketLedgerHeader(
                onOpenLlm = { showLlmDialog = true },
                llmState = llmState
            )
        },
        bottomBar = {
            PocketLedgerBottomBar(
                selectedTab = selectedTab,
                onTabSelect = { selectedTab = it }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTab != 0) selectedTab = 0
                    showAddEntry = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add entry"
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (!notificationAccess) {
                NotificationAccessQuietBanner(
                    onEnable = {
                        runCatching {
                            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                        }
                    }
                )
            }

            Box(Modifier.weight(1f)) {
                if (selectedTab == 0) {
                    TransactionsRoute(
                        showAddEntry = showAddEntry,
                        onAddEntryDismiss = { showAddEntry = false }
                    )
                } else {
                    AutopaysRoute()
                }
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

/**
 * Top App Header:
 * - Left-aligned screen title in Newsreader Serif ("Pocket Ledger")
 * - Optional offline AI fallback status badge/button
 */
@Composable
private fun PocketLedgerHeader(
    onOpenLlm: () -> Unit,
    llmState: LlmModelState
) {
    val extras = LocalLedgerExtras.current

    Surface(
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 16.dp)
        ) {
            // Title row: Screen title + Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pocket Ledger",
                    style = ScreenTitleStyle,
                    color = MaterialTheme.colorScheme.onBackground
                )

                TextButton(onClick = onOpenLlm) {
                    val label = when (llmState) {
                        LlmModelState.Ready -> "AI Ready"
                        is LlmModelState.Downloading -> "AI …"
                        else -> "AI"
                    }
                    Text(
                        text = label,
                        color = extras.mutedText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            HairlineDivider(color = extras.hairline)
        }
    }
}

/**
 * Bottom Navigation Bar maintaining the current new design aesthetics.
 */
@Composable
private fun PocketLedgerBottomBar(
    selectedTab: Int,
    onTabSelect: (Int) -> Unit
) {
    val extras = LocalLedgerExtras.current

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            HairlineDivider(color = extras.hairline)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavTabItem(
                    title = "Transactions",
                    icon = Icons.Filled.List,
                    selected = selectedTab == 0,
                    onClick = { onTabSelect(0) }
                )
                BottomNavTabItem(
                    title = "Autopays",
                    icon = Icons.Filled.Refresh,
                    selected = selectedTab == 1,
                    onClick = { onTabSelect(1) }
                )
            }
            HairlineDivider(color = extras.hairline.copy(alpha = 0.5f))
            PrivacyFooter()
        }
    }
}

@Composable
private fun BottomNavTabItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val extras = LocalLedgerExtras.current
    val accentColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .height(2.5.dp)
                .width(if (selected) 36.dp else 0.dp)
                .background(if (selected) accentColor else androidx.compose.ui.graphics.Color.Transparent)
        )
        Spacer(Modifier.height(4.dp))
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (selected) accentColor else extras.mutedText,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.onBackground else extras.mutedText
        )
    }
}

/**
 * Quiet, hairline-bordered alert banner for notification access permission.
 */
@Composable
private fun NotificationAccessQuietBanner(onEnable: () -> Unit) {
    val extras = LocalLedgerExtras.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, extras.warning.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Notification access inactive",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Enable access to log payments automatically from SMS & bank alerts.",
                    style = MaterialTheme.typography.bodySmall,
                    color = extras.mutedText
                )
            }
            Spacer(Modifier.width(12.dp))
            TextButton(onClick = onEnable) {
                Text("Enable", color = extras.warning, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

/**
 * On-device AI fallback dialog.
 */
@Composable
private fun LlmModelDialog(
    state: LlmModelState,
    onDownload: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val extras = LocalLedgerExtras.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("On-device AI fallback", style = MaterialTheme.typography.titleMedium) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Qwen2.5 0.5B can read unusual bank alerts that the normal parser misses. Amounts still come straight from the notification text.",
                    style = MaterialTheme.typography.bodySmall,
                    color = extras.mutedText
                )
                when (state) {
                    LlmModelState.Missing -> Text(
                        text = "Model not downloaded (${LlmModelManager.MODEL_SIZE_LABEL}).",
                        style = MaterialTheme.typography.bodySmall,
                        color = extras.mutedText
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
                            style = MaterialTheme.typography.bodySmall,
                            color = extras.mutedText
                        )
                    }

                    LlmModelState.Ready -> Text(
                        text = "Ready. It unloads from memory after a minute of disuse.",
                        style = MaterialTheme.typography.bodySmall,
                        color = extras.credit
                    )

                    is LlmModelState.Failed -> Text(
                        text = "Download failed: ${state.message}",
                        style = MaterialTheme.typography.bodySmall,
                        color = extras.danger
                    )
                }
            }
        },
        confirmButton = {
            when (state) {
                LlmModelState.Missing, is LlmModelState.Failed -> TextButton(onClick = onDownload) {
                    Text("Download", color = MaterialTheme.colorScheme.primary)
                }

                LlmModelState.Ready -> TextButton(onClick = onDelete) {
                    Text("Remove", color = extras.danger)
                }

                is LlmModelState.Downloading -> TextButton(onClick = onDismiss) {
                    Text("Hide", color = extras.mutedText)
                }
            }
        },
        dismissButton = {
            if (state !is LlmModelState.Downloading) {
                TextButton(onClick = onDismiss) {
                    Text("Close", color = extras.mutedText)
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

/**
 * Slim privacy line at the foot of Home per DESIGN.md Section 5.1:
 * "Stored on this device only. Small, muted, no lock-shield illustration."
 */
@Composable
private fun PrivacyFooter() {
    val extras = LocalLedgerExtras.current

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            HairlineDivider()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Stored on this device only · No servers, no bank logins",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = extras.mutedText,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
