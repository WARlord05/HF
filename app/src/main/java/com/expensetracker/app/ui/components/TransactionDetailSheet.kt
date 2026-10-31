package com.expensetracker.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expensetracker.app.data.TransactionEntity
import com.expensetracker.app.ui.formatDate
import com.expensetracker.app.ui.formatMoney
import com.expensetracker.app.ui.theme.DisplayTotalStyle
import com.expensetracker.app.ui.theme.LocalLedgerExtras
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailSheet(
    transaction: TransactionEntity,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val extras = LocalLedgerExtras.current
    var showRawSource by remember { mutableStateOf(false) }

    val isCredit = transaction.transactionType == "CREDIT"
    val sign = if (isCredit) "+" else "−"
    val timeStr = runCatching {
        Instant.ofEpochMilli(transaction.timestamp)
            .atZone(ZoneId.systemDefault())
            .format(timeFormatter)
    }.getOrDefault("")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Monogram + Merchant Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Monogram(category = transaction.category, name = transaction.merchantName, size = 48.dp)
                Column(Modifier.weight(1f)) {
                    Text(
                        text = transaction.merchantName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${formatDate(transaction.timestamp)} · $timeStr",
                        style = MaterialTheme.typography.bodySmall,
                        color = extras.mutedText
                    )
                }
            }

            // Display Amount
            Column(
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                SectionLabel(
                    text = if (isCredit) "Amount received" else "Amount debited"
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "$sign${formatMoney(transaction.amount, transaction.currency)}",
                    style = DisplayTotalStyle,
                    color = if (isCredit) extras.credit else MaterialTheme.colorScheme.onBackground
                )
            }

            HairlineDivider()

            // Details rows
            DetailRow(label = "Category", value = transaction.category)
            DetailRow(label = "Account / source", value = transaction.accountSource)
            DetailRow(label = "Type", value = if (isCredit) "Credit" else "Debit")
            if (transaction.isAutopay) {
                DetailRow(label = "Recurring", value = "Autopay Mandate")
            }

            // Raw notification source transparency (collapsed by default)
            if (!transaction.rawNotificationText.isNullOrBlank()) {
                HairlineDivider()
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showRawSource = !showRawSource }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SectionLabel(text = "Notification source")
                        Text(
                            text = if (showRawSource) "Hide" else "Show",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    AnimatedVisibility(visible = showRawSource) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = transaction.rawNotificationText,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                ),
                                color = extras.mutedText
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Delete", color = extras.danger)
                }

                Button(
                    onClick = onEdit,
                    modifier = Modifier.weight(1.5f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Edit entry", fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = LocalLedgerExtras.current.mutedText
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
