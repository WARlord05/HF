package com.expensetracker.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.expensetracker.app.data.AutopayEntity
import com.expensetracker.app.data.TransactionEntity
import com.expensetracker.app.data.displayStatus
import com.expensetracker.app.parser.BillingCycle
import com.expensetracker.app.ui.components.EmptyState
import com.expensetracker.app.ui.components.HairlineDivider
import com.expensetracker.app.ui.components.Monogram
import com.expensetracker.app.ui.components.SectionLabel
import com.expensetracker.app.ui.components.StatusBadge
import com.expensetracker.app.ui.components.SummaryBlock
import com.expensetracker.app.ui.theme.LocalLedgerExtras
import com.expensetracker.app.ui.theme.RowTitleStyle
import kotlinx.coroutines.flow.Flow

@Composable
fun AutopaysRoute(viewModel: AutopaysViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AutopaysScreen(
        state = state,
        onCancel = viewModel::cancel,
        onReactivate = viewModel::reactivate,
        historyFor = viewModel::historyFor
    )
}

@Composable
fun AutopaysScreen(
    state: AutopaysUiState,
    onCancel: (AutopayEntity) -> Unit,
    onReactivate: (AutopayEntity) -> Unit,
    historyFor: (Long) -> Flow<List<TransactionEntity>>
) {
    val extras = LocalLedgerExtras.current
    val now = System.currentTimeMillis()

    // Group autopays by status per DESIGN.md Section 5.3:
    // UPCOMING (next 7 days), ACTIVE, STALE / CANCELLED
    val upcomingItems = remember(state.autopays) {
        state.autopays.filter { displayStatus(it, now) == "UPCOMING" }
    }
    val activeItems = remember(state.autopays) {
        state.autopays.filter { displayStatus(it, now) == "ACTIVE" }
    }
    val staleOrCancelledItems = remember(state.autopays) {
        state.autopays.filter {
            val status = displayStatus(it, now)
            status == "STALE" || status == "CANCELLED" || status == "PAUSED"
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Flat, hairline-bordered summary block
        item {
            SummaryBlock(
                monthlyTotal = state.monthlyTotal,
                upcomingCount = state.upcomingCount,
                upcomingTotal = state.upcomingTotal,
                activeCount = state.activeCount,
                currency = state.currency
            )
        }

        if (state.autopays.isEmpty()) {
            item {
                EmptyState(
                    title = "No autopays detected yet",
                    message = "Subscription and recurring mandate notifications will appear here automatically."
                )
            }
        } else {
            // Group 1: UPCOMING (next 7 days)
            if (upcomingItems.isNotEmpty()) {
                item {
                    SectionLabel(text = "Upcoming (next 7 days)")
                }
                items(items = upcomingItems, key = { it.id }) { autopay ->
                    AutopayItem(
                        autopay = autopay,
                        currency = state.currency,
                        history = historyFor(autopay.id),
                        onCancel = { onCancel(autopay) },
                        onReactivate = { onReactivate(autopay) }
                    )
                    HairlineDivider()
                }
            }

            // Group 2: ACTIVE
            if (activeItems.isNotEmpty()) {
                item {
                    SectionLabel(text = "Active subscriptions")
                }
                items(items = activeItems, key = { it.id }) { autopay ->
                    AutopayItem(
                        autopay = autopay,
                        currency = state.currency,
                        history = historyFor(autopay.id),
                        onCancel = { onCancel(autopay) },
                        onReactivate = { onReactivate(autopay) }
                    )
                    HairlineDivider()
                }
            }

            // Group 3: STALE / CANCELLED (55% opacity per DESIGN.md)
            if (staleOrCancelledItems.isNotEmpty()) {
                item {
                    SectionLabel(text = "Stale / Cancelled")
                }
                items(items = staleOrCancelledItems, key = { it.id }) { autopay ->
                    AutopayItem(
                        autopay = autopay,
                        currency = state.currency,
                        history = historyFor(autopay.id),
                        onCancel = { onCancel(autopay) },
                        onReactivate = { onReactivate(autopay) },
                        modifier = Modifier.alpha(0.55f)
                    )
                    HairlineDivider()
                }
            }
        }

        item { Spacer(Modifier.height(48.dp)) }
    }
}

/**
 * Clean flat autopay row with category monogram, service name, next payment, status badge,
 * and expandable debit history.
 */
@Composable
private fun AutopayItem(
    autopay: AutopayEntity,
    currency: String,
    history: Flow<List<TransactionEntity>>,
    onCancel: () -> Unit,
    onReactivate: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable(autopay.id) { mutableStateOf(false) }
    val transactions by remember(autopay.id) { history }
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val status = displayStatus(autopay)
    val isCancelled = autopay.status == "CANCELLED"
    val extras = LocalLedgerExtras.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Monogram(category = autopay.category, name = autopay.serviceName, size = 40.dp)

            Spacer(Modifier.width(14.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = autopay.serviceName,
                    style = RowTitleStyle,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${BillingCycle.label(autopay.billingCycle)} · last ${formatMoney(autopay.estimatedAmount, currency)} on ${formatDate(autopay.lastChargedTimestamp)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = extras.mutedText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(horizontalAlignment = Alignment.End) {
                StatusBadge(status)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Next ${formatDate(autopay.nextExpectedTimestamp)}",
                    style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                    color = extras.mutedText
                )
            }
        }

        // Expandable Debit History & Actions
        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, start = 54.dp)
            ) {
                HairlineDivider()
                Spacer(Modifier.height(8.dp))

                SectionLabel(text = "Debit History")
                Spacer(Modifier.height(4.dp))

                if (transactions.isEmpty()) {
                    Text(
                        text = "No prior debits recorded under this mandate.",
                        style = MaterialTheme.typography.bodySmall,
                        color = extras.mutedText,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                } else {
                    transactions.take(5).forEach { tx ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = formatDate(tx.timestamp),
                                style = MaterialTheme.typography.bodySmall,
                                color = extras.mutedText
                            )
                            Text(
                                text = "−${formatMoney(tx.amount, tx.currency)}",
                                style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (isCancelled) {
                        TextButton(onClick = onReactivate) {
                            Text("Reactivate mandate", color = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        TextButton(onClick = onCancel) {
                            Text("Mark cancelled", color = extras.danger)
                        }
                    }
                }
            }
        }
    }
}
