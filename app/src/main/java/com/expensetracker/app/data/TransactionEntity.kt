package com.expensetracker.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val merchantName: String,
    val amount: Double,
    val currency: String = "USD",
    val timestamp: Long,
    val category: String,
    val accountSource: String,
    val transactionType: String,
    val isAutopay: Boolean = false,
    val autopayId: Long? = null,
    val rawNotificationText: String? = null
)
