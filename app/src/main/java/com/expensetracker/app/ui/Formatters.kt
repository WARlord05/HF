package com.expensetracker.app.ui

import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Currency
import java.util.Locale

private val dayHeaderFormatter = DateTimeFormatter.ofPattern("d MMM, EEE")
private val shortDateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")
private val monthHeaderFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

fun defaultCurrencyCode(): String =
    runCatching { Currency.getInstance(Locale.getDefault()).currencyCode }.getOrDefault("INR")

/**
 * Formats a raw numeric money value with currency symbol based on current locale.
 */
fun formatMoney(amount: Double, currency: String = defaultCurrencyCode()): String {
    val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
    runCatching { Currency.getInstance(currency) }
        .getOrNull()?.let { format.currency = it }
    format.maximumFractionDigits = 2
    format.minimumFractionDigits = if (amount % 1.0 == 0.0) 0 else 2
    return format.format(amount)
}

/**
 * Formats signed currency using true minus '−' (U+2212) or '+'.
 */
fun formatSignedMoney(amount: Double, currency: String, isCredit: Boolean): String {
    val base = formatMoney(amount, currency)
    val sign = if (isCredit) "+" else "−"
    return "$sign$base"
}

fun toDate(millis: Long): LocalDate =
    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()

fun formatDate(millis: Long): String = shortDateFormatter.format(toDate(millis))

fun formatMonth(date: LocalDate = LocalDate.now()): String =
    monthHeaderFormatter.format(date)

fun formatDayHeader(date: LocalDate): String {
    val today = LocalDate.now()
    return when (date) {
        today -> "TODAY"
        today.minusDays(1) -> "YESTERDAY"
        else -> dayHeaderFormatter.format(date).uppercase(Locale.getDefault())
    }
}
