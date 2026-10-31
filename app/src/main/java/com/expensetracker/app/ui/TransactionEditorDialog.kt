package com.expensetracker.app.ui

import androidx.compose.runtime.Composable
import com.expensetracker.app.data.TransactionEntity
import com.expensetracker.app.ui.components.EntrySheet

/**
 * Backward compatibility wrapper delegating to EntrySheet.
 */
@Composable
fun TransactionEditorDialog(
    initial: TransactionEntity?,
    onDismiss: () -> Unit,
    onSave: (TransactionEntity) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    EntrySheet(
        initial = initial,
        onDismiss = onDismiss,
        onSave = onSave,
        onDelete = onDelete
    )
}
