package com.expensetracker.app.parser

import com.expensetracker.app.data.TransactionEntity

data class AmountMatch(
    val amount: Double,
    val currency: String,
    val range: IntRange
)

data class ParsedNotification(
    val merchantName: String,
    val amount: Double,
    val currency: String,
    val timestamp: Long,
    val category: String,
    val accountSource: String,
    val transactionType: String,
    val isAutopay: Boolean,
    val billingCycle: String
) {
    fun toEntity(autopayId: Long?, rawText: String) = TransactionEntity(
        merchantName = merchantName,
        amount = amount,
        currency = currency,
        timestamp = timestamp,
        category = category,
        accountSource = accountSource,
        transactionType = transactionType,
        isAutopay = isAutopay,
        autopayId = autopayId,
        rawNotificationText = rawText.take(500)
    )
}
