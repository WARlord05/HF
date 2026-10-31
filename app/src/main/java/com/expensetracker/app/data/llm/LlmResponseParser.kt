package com.expensetracker.app.data.llm

import com.expensetracker.app.parser.AmountMatch
import com.expensetracker.app.parser.ParsedNotification
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONObject

@Singleton
class LlmResponseParser @Inject constructor() {

    fun parse(
        response: String,
        amount: AmountMatch,
        postedAt: Long,
        accountSource: String
    ): ParsedNotification? {
        val json = extractJson(response) ?: return null
        val obj = runCatching { JSONObject(json) }.getOrNull() ?: return null
        if (!obj.flag("is_transaction")) return null

        val type = obj.optString("type").trim().uppercase()
        if (type != "DEBIT" && type != "CREDIT") return null

        val autopay = type == "DEBIT" && obj.flag("autopay")
        return ParsedNotification(
            merchantName = sanitizeMerchant(obj.optString("merchant")),
            amount = amount.amount,
            currency = amount.currency,
            timestamp = postedAt,
            category = normalize(obj.optString("category"), CATEGORIES) ?: "Other",
            accountSource = accountSource,
            transactionType = type,
            isAutopay = autopay,
            billingCycle = if (autopay) {
                normalize(obj.optString("cycle"), CYCLES) ?: "MONTHLY"
            } else {
                "MONTHLY"
            }
        )
    }

    private fun extractJson(response: String): String? {
        val trimmed = response.take(MAX_RESPONSE_CHARS)
        val start = trimmed.indexOf('{')
        if (start == -1) return null
        val end = trimmed.indexOf('}', start)
        if (end <= start) return null
        return trimmed.substring(start, end + 1)
    }

    private fun JSONObject.flag(key: String): Boolean {
        val value = opt(key) ?: return false
        return value == true || value.toString().equals("true", ignoreCase = true)
    }

    private fun sanitizeMerchant(raw: String): String {
        val value = raw
            .replace(CONTROL_CHARS, " ")
            .replace(WHITESPACE, " ")
            .trim()
            .take(48)
        return value.takeIf { it.length >= 2 && it.any { char -> char.isLetter() } } ?: "Unknown"
    }

    private fun normalize(raw: String, allowed: Set<String>): String? =
        allowed.firstOrNull { it.equals(raw.trim(), ignoreCase = true) }

    companion object {
        private const val MAX_RESPONSE_CHARS = 2_000
        private val CONTROL_CHARS = Regex("\\p{Cntrl}")
        private val WHITESPACE = Regex("\\s+")
        private val CATEGORIES = setOf(
            "Entertainment", "Food", "Travel", "Bills",
            "Health", "Subscription", "Shopping", "Other"
        )
        private val CYCLES = setOf("MONTHLY", "YEARLY", "QUARTERLY", "WEEKLY")
    }
}
