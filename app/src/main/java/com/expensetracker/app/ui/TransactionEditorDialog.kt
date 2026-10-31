package com.expensetracker.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.expensetracker.app.data.TransactionEntity

private val categories = listOf(
    "Food", "Shopping", "Travel", "Bills",
    "Entertainment", "Health", "Subscription", "Other"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionEditorDialog(
    initial: TransactionEntity?,
    onDismiss: () -> Unit,
    onSave: (TransactionEntity) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var merchant by remember(initial) { mutableStateOf(initial?.merchantName.orEmpty()) }
    var amount by remember(initial) { mutableStateOf(initial?.amount?.let(::plainAmount).orEmpty()) }
    var type by remember(initial) { mutableStateOf(initial?.transactionType ?: "DEBIT") }
    var category by remember(initial) { mutableStateOf(initial?.category ?: "Other") }
    var account by remember(initial) { mutableStateOf(initial?.accountSource ?: "Cash") }

    val amountValue = amount.replace(",", "").toDoubleOrNull()
        ?.takeIf { it.isFinite() && it > 0.0 && it < 100_000_000.0 }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Add transaction" else "Edit transaction") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = merchant,
                    onValueChange = { merchant = it },
                    label = { Text("Merchant") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { input ->
                        amount = input.filter { it.isDigit() || it == '.' || it == ',' }
                    },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == "DEBIT",
                        onClick = { type = "DEBIT" },
                        label = { Text("Debit") }
                    )
                    FilterChip(
                        selected = type == "CREDIT",
                        onClick = { type = "CREDIT" },
                        label = { Text("Credit") }
                    )
                }
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { item ->
                        FilterChip(
                            selected = category == item,
                            onClick = { category = item },
                            label = { Text(item) }
                        )
                    }
                }
                OutlinedTextField(
                    value = account,
                    onValueChange = { account = it },
                    label = { Text("Account / source") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = merchant.isNotBlank() && amountValue != null,
                onClick = {
                    val value = amountValue ?: return@TextButton
                    onSave(
                        TransactionEntity(
                            id = initial?.id ?: 0,
                            merchantName = merchant.trim(),
                            amount = value,
                            currency = initial?.currency ?: "USD",
                            timestamp = initial?.timestamp ?: System.currentTimeMillis(),
                            category = category,
                            accountSource = account.ifBlank { "Cash" },
                            transactionType = type,
                            isAutopay = initial?.isAutopay ?: false,
                            autopayId = initial?.autopayId,
                            rawNotificationText = initial?.rawNotificationText
                        )
                    )
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

private fun plainAmount(value: Double): String =
    if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()
