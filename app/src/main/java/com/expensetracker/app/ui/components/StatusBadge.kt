package com.expensetracker.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expensetracker.app.ui.theme.LocalLedgerExtras

/**
 * Text-only status badge pill with muted tinted background.
 */
@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val extras = LocalLedgerExtras.current
    val normalized = status.uppercase()

    val (bg, fg) = when (normalized) {
        "ACTIVE" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.14f) to MaterialTheme.colorScheme.primary
        "UPCOMING" -> extras.warning.copy(alpha = 0.16f) to extras.warning
        "CANCELLED", "STALE" -> extras.danger.copy(alpha = 0.14f) to extras.danger
        else -> MaterialTheme.colorScheme.surfaceVariant to extras.mutedText
    }

    val label = when (normalized) {
        "ACTIVE" -> "Active"
        "UPCOMING" -> "Upcoming"
        "CANCELLED" -> "Cancelled"
        "STALE" -> "Stale"
        "PAUSED" -> "Paused"
        else -> status
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            color = fg,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 14.sp
        )
    }
}
