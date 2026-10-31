package com.expensetracker.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.expensetracker.app.data.TransactionEntity
import com.expensetracker.app.ui.components.AmountText
import com.expensetracker.app.ui.components.EmptyState
import com.expensetracker.app.ui.components.EntrySheet
import com.expensetracker.app.ui.components.HairlineDivider
import com.expensetracker.app.ui.components.Monogram
import com.expensetracker.app.ui.components.SectionLabel
import com.expensetracker.app.ui.components.TransactionDetailSheet
import com.expensetracker.app.ui.theme.LocalLedgerExtras
import com.expensetracker.app.ui.theme.MonthHeaderStyle
import com.expensetracker.app.ui.theme.RowTitleStyle

@Composable
fun TransactionsRoute(
    viewModel: TransactionsViewModel = hiltViewModel(),
    showAddEntry: Boolean = false,
    onAddEntryDismiss: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TransactionsScreen(
        state = state,
        onQueryChange = viewModel::setQuery,
        onCategoryChange = viewModel::setCategory,
        onRangeChange = viewModel::setRange,
        onSave = viewModel::save,
        onDelete = viewModel::delete,
        showAddEntry = showAddEntry,
        onAddEntryDismiss = onAddEntryDismiss
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    state: TransactionsUiState,
    onQueryChange: (String) -> Unit,
    onCategoryChange: (String?) -> Unit,
    onRangeChange: (DateRange) -> Unit,
    onSave: (TransactionEntity) -> Unit,
    onDelete: (TransactionEntity) -> Unit,
    showAddEntry: Boolean = false,
    onAddEntryDismiss: () -> Unit = {}
) {
    val extras = LocalLedgerExtras.current
    var viewingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var isAddingTransaction by remember { mutableStateOf(false) }

    if (showAddEntry) {
        isAddingTransaction = true
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 64.dp)
        ) {
            // Month Header & Summary Block
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = formatMonth(),
                        style = MonthHeaderStyle,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Spent ${formatMoney(state.spent, state.currency)} · Received ${formatMoney(state.received, state.currency)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = extras.mutedText
                    )
                }
            }

            // Search Bar & Filter Chips
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = onQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp),
                        placeholder = {
                            Text(
                                "Search merchant, account, category...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = extras.mutedText
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Search,
                                contentDescription = null,
                                tint = extras.mutedText,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = extras.hairline,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    // Date range chips
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(DateRange.entries) { range ->
                            val selected = state.range == range
                            FilterChip(
                                selected = selected,
                                onClick = { onRangeChange(range) },
                                label = { Text(range.label, fontSize = 13.sp) },
                                shape = CircleShape,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    selectedLabelColor = MaterialTheme.colorScheme.primary,
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    labelColor = extras.mutedText
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selected,
                                    borderColor = extras.hairline,
                                    selectedBorderColor = MaterialTheme.colorScheme.primary,
                                    borderWidth = 1.dp
                                )
                            )
                        }
                    }

                    // Category filter chips
                    if (state.categories.isNotEmpty()) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                val isAll = state.category == null
                                FilterChip(
                                    selected = isAll,
                                    onClick = { onCategoryChange(null) },
                                    label = { Text("All Categories", fontSize = 13.sp) },
                                    shape = CircleShape,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        selectedLabelColor = MaterialTheme.colorScheme.primary,
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        labelColor = extras.mutedText
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isAll,
                                        borderColor = extras.hairline,
                                        selectedBorderColor = MaterialTheme.colorScheme.primary,
                                        borderWidth = 1.dp
                                    )
                                )
                            }
                            items(state.categories) { cat ->
                                val selected = state.category.equals(cat, ignoreCase = true)
                                FilterChip(
                                    selected = selected,
                                    onClick = { onCategoryChange(if (selected) null else cat) },
                                    label = { Text(cat, fontSize = 13.sp) },
                                    shape = CircleShape,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        selectedLabelColor = MaterialTheme.colorScheme.primary,
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        labelColor = extras.mutedText
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = selected,
                                        borderColor = extras.hairline,
                                        selectedBorderColor = MaterialTheme.colorScheme.primary,
                                        borderWidth = 1.dp
                                    )
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    HairlineDivider()
                }
            }

            // Timeline List with Sticky Day Headers
            if (state.groups.isEmpty()) {
                item {
                    EmptyState(
                        title = "Nothing here yet",
                        message = "New payments appear as they arrive from bank notifications.",
                        actionLabel = "Add cash entry",
                        onAction = { isAddingTransaction = true }
                    )
                }
            } else {
                state.groups.forEach { group ->
                    stickyHeader(key = group.date.toString()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SectionLabel(text = formatDayHeader(group.date))
                            if (group.spent > 0) {
                                Text(
                                    text = formatMoney(group.spent, state.currency),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFeatureSettings = "tnum"
                                    ),
                                    color = extras.mutedText
                                )
                            }
                        }
                    }

                    items(items = group.transactions, key = { it.id }) { transaction ->
                        TransactionRow(
                            transaction = transaction,
                            onClick = { viewingTransaction = transaction }
                        )
                        HairlineDivider(modifier = Modifier.padding(start = 76.dp, end = 20.dp))
                    }
                }
            }
        }
    }

    // Detail Bottom Sheet
    viewingTransaction?.let { transaction ->
        TransactionDetailSheet(
            transaction = transaction,
            onDismiss = { viewingTransaction = null },
            onEdit = {
                val current = transaction
                viewingTransaction = null
                editingTransaction = current
            },
            onDelete = {
                onDelete(transaction)
                viewingTransaction = null
            }
        )
    }

    // Edit Bottom Sheet
    editingTransaction?.let { transaction ->
        EntrySheet(
            initial = transaction,
            onDismiss = { editingTransaction = null },
            onSave = {
                onSave(it)
                editingTransaction = null
            },
            onDelete = {
                onDelete(transaction)
                editingTransaction = null
            }
        )
    }

    // Add Entry Sheet
    if (isAddingTransaction) {
        EntrySheet(
            initial = null,
            onDismiss = {
                isAddingTransaction = false
                onAddEntryDismiss()
            },
            onSave = {
                onSave(it)
                isAddingTransaction = false
                onAddEntryDismiss()
            }
        )
    }
}

/**
 * 64dp flat transaction row with left monogram, title and caption stacked, amount right-aligned.
 */
@Composable
private fun TransactionRow(
    transaction: TransactionEntity,
    onClick: () -> Unit
) {
    val extras = LocalLedgerExtras.current
    val isCredit = transaction.transactionType == "CREDIT"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Monogram (40dp circle in category tint)
        Monogram(
            category = transaction.category,
            name = transaction.merchantName,
            size = 40.dp
        )

        Spacer(Modifier.width(16.dp))

        // Title & Caption
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = transaction.merchantName,
                style = RowTitleStyle,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${transaction.category} · ${transaction.accountSource}",
                    style = MaterialTheme.typography.bodySmall,
                    color = extras.mutedText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (transaction.isAutopay) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Autopay",
                        modifier = Modifier.size(13.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(Modifier.width(12.dp))

        // Right-aligned Tabular Amount
        AmountText(
            amount = transaction.amount,
            isCredit = isCredit,
            currency = transaction.currency
        )
    }
}
