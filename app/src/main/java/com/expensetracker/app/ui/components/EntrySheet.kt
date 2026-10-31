package com.expensetracker.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expensetracker.app.data.TransactionEntity
import com.expensetracker.app.ui.defaultCurrencyCode
import com.expensetracker.app.ui.theme.CategoryColors
import com.expensetracker.app.ui.theme.DisplayTotalStyle
import com.expensetracker.app.ui.theme.LocalLedgerExtras

private val categories = listOf(
    "Food", "Shopping", "Travel", "Utility",
    "Subscriptions", "Entertainment", "Health", "Other"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntrySheet(
    initial: TransactionEntity?,
    onDismiss: () -> Unit,
    onSave: (TransactionEntity) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val extras = LocalLedgerExtras.current

    var merchant by remember(initial) { mutableStateOf(initial?.merchantName.orEmpty()) }
    var amount by remember(initial) { mutableStateOf(initial?.amount?.let(::plainAmount).orEmpty()) }
    var type by remember(initial) { mutableStateOf(initial?.transactionType ?: "DEBIT") }
    var category by remember(initial) { mutableStateOf(initial?.category ?: "Food") }
    var account by remember(initial) { mutableStateOf(initial?.accountSource ?: "Cash") }

    val amountValue = amount.replace(",", "").toDoubleOrNull()
        ?.takeIf { it.isFinite() && it > 0.0 && it < 100_000_000.0 }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (initial == null) "New entry" else "Edit entry",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = extras.mutedText)
                }
            }

            // Type Toggle (Debit / Credit segmented switch)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(2.dp)
            ) {
                val debitSelected = type == "DEBIT"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (debitSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { type = "DEBIT" }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Debit (Expense)",
                        fontSize = 13.sp,
                        fontWeight = if (debitSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (debitSelected) MaterialTheme.colorScheme.onBackground else extras.mutedText
                    )
                }

                val creditSelected = type == "CREDIT"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (creditSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { type = "CREDIT" }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Credit (Income)",
                        fontSize = 13.sp,
                        fontWeight = if (creditSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (creditSelected) extras.credit else extras.mutedText
                    )
                }
            }

            // Amount Input with Display typography
            OutlinedTextField(
                value = amount,
                onValueChange = { input ->
                    amount = input.filter { it.isDigit() || it == '.' || it == ',' }
                },
                label = { Text("Amount") },
                textStyle = DisplayTotalStyle.copy(fontSize = 32.sp, lineHeight = 36.sp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = extras.hairline
                )
            )

            // Merchant / Description
            OutlinedTextField(
                value = merchant,
                onValueChange = { merchant = it },
                label = { Text("Merchant / Description") },
                placeholder = { Text("e.g. Swiggy, Metro, Netflix") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = extras.hairline
                )
            )

            // Category Chips
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionLabel(text = "Category")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { item ->
                        val selected = category.equals(item, ignoreCase = true)
                        val catColor = CategoryColors.colorFor(item)
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    if (selected) catColor.copy(alpha = 0.16f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (selected) catColor else extras.hairline,
                                    shape = CircleShape
                                )
                                .clickable { category = item }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = item,
                                fontSize = 13.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (selected) catColor else extras.mutedText
                            )
                        }
                    }
                }
            }

            // Account / Source
            OutlinedTextField(
                value = account,
                onValueChange = { account = it },
                label = { Text("Account / Source") },
                placeholder = { Text("e.g. Cash, HDFC ••4091, UPI") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = extras.hairline
                )
            )

            Spacer(Modifier.height(8.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onDelete != null) {
                    TextButton(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Delete", color = extras.danger)
                    }
                }

                Button(
                    onClick = {
                        val value = amountValue ?: return@Button
                        onSave(
                            TransactionEntity(
                                id = initial?.id ?: 0,
                                merchantName = merchant.trim(),
                                amount = value,
                                currency = initial?.currency ?: defaultCurrencyCode(),
                                timestamp = initial?.timestamp ?: System.currentTimeMillis(),
                                category = category,
                                accountSource = account.ifBlank { "Cash" },
                                transactionType = type,
                                isAutopay = initial?.isAutopay ?: false,
                                autopayId = initial?.autopayId,
                                rawNotificationText = initial?.rawNotificationText
                            )
                        )
                    },
                    enabled = merchant.isNotBlank() && amountValue != null,
                    modifier = Modifier.weight(if (onDelete != null) 2f else 1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Save entry", fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

private fun plainAmount(value: Double): String =
    if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()
