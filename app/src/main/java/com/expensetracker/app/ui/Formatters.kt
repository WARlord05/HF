package com.expensetracker.app.ui

import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Currency
import java.util.Locale

private val dayFormatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy")
private val shortDateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")

fun formatMoney(amount: Double, currency: String): String {
    val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
    format.currency = runCatching { Currency.getInstance(currency) }
        .getOrDefault(Currency.getInstance("USD"))
    format.maximumFractionDigits = 2
    format.minimumFractionDigits = if (amount % 1.0 == 0.0) 0 else 2
    return format.format(amount)
}

fun toDate(millis: Long): LocalDate =
    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()

fun formatDate(millis: Long): String = shortDateFormatter.format(toDate(millis))

fun formatBytes(bytes: Long): String = when {
    bytes >= 1_000_000_000 -> String.format(Locale.US, "%.1f GB", bytes / 1_000_000_000.0)
    bytes >= 1_000_000 -> String.format(Locale.US, "%.0f MB", bytes / 1_000_000.0)
    else -> String.format(Locale.US, "%.0f KB", bytes / 1_000.0)
}

fun formatDayHeader(date: LocalDate): String {
    val today = LocalDate.now()
    return when (date) {
        today -> "Today"
        today.minusDays(1) -> "Yesterday"
        else -> dayFormatter.format(date)
    }
}
