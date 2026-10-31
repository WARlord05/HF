package com.expensetracker.app.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.expensetracker.app.ui.formatMoney
import com.expensetracker.app.ui.theme.DisplayTotalStyle
import com.expensetracker.app.ui.theme.LocalLedgerExtras

/**
 * Summary block at top of Autopays screen.
 * Flat, hairline-bordered, warm paper surface.
 * Shows 'MONTHLY COMMITMENT' label, large Display total, and stats for upcoming and active mandates.
 */
@Composable
fun SummaryBlock(
    monthlyTotal: Double,
    upcomingCount: Int,
    upcomingTotal: Double,
    activeCount: Int,
    currency: String,
    modifier: Modifier = Modifier
) {
    val extras = LocalLedgerExtras.current

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, extras.hairline, RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            SectionLabel(
                text = "Monthly commitment",
                color = extras.mutedText
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = formatMoney(monthlyTotal, currency),
                style = DisplayTotalStyle,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(16.dp))
            HairlineDivider()
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Upcoming $upcomingCount debits · ${formatMoney(upcomingTotal, currency)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = extras.mutedText
                )
                Text(
                    text = "Active $activeCount",
                    style = MaterialTheme.typography.bodySmall,
                    color = extras.mutedText
                )
            }
        }
    }
}
