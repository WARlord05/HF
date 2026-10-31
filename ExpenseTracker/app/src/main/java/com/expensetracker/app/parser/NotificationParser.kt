package com.expensetracker.app.parser

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pure, on-device parser for bank / UPI / wallet notification text.
 * No network, no Android dependencies, so it is unit testable.
 */
@Singleton
class NotificationParser @Inject constructor() {

    fun parse(text: String, postedAt: Long, sourcePackage: String = ""): ParsedNotification? {
        val clean = text.replace('\n', ' ').replace(WHITESPACE, " ").trim().take(MAX_TEXT_LENGTH)
        if (clean.length < 6) return null

        val lower = clean.lowercase()
        val type = when {
            DEBIT_WORDS.containsMatchIn(lower) -> "DEBIT"
            CREDIT_WORDS.containsMatchIn(lower) -> "CREDIT"
            else -> return null
        }

        val amountMatch = CURRENCY_FIRST.find(clean) ?: AMOUNT_FIRST.find(clean) ?: return null
        val amount = amountMatch.groupValues[1].replace(",", "").toDoubleOrNull()
            ?.takeIf { it.isFinite() && it > 0.0 && it < MAX_AMOUNT }
            ?: return null
        val currency = detectCurrency(amountMatch.value)

        val withoutAmount = clean.removeRange(amountMatch.range).replace(WHITESPACE, " ").trim()
        val isAutopay = type == "DEBIT" && AUTOPAY_WORDS.containsMatchIn(lower)
        val merchant = extractMerchant(withoutAmount, type)
            .ifBlank { if (isAutopay) "Recurring payment" else "Unknown" }
        val category = classify(clean, merchant)

        return ParsedNotification(
            merchantName = merchant,
            amount = amount,
            currency = currency,
            timestamp = postedAt,
            category = category,
            accountSource = extractAccount(clean, sourcePackage),
            transactionType = type,
            isAutopay = isAutopay,
            billingCycle = if (isAutopay) detectCycle(lower) else "MONTHLY"
        )
    }

    private fun extractMerchant(text: String, type: String): String {
        val patterns = if (type == "CREDIT") CREDIT_MERCHANT_PATTERNS else DEBIT_MERCHANT_PATTERNS
        for (pattern in patterns) {
            val match = pattern.find(text) ?: continue
            val candidate = cleanMerchant(match.groupValues[1])
            if (candidate.isNotBlank()) return candidate
        }
        return ""
    }

    private fun cleanMerchant(raw: String): String {
        var value = raw.substringBefore('@').trim()
        value = value.trim(' ', ',', '.', ';', ':', '-', '|', '(', ')')
        value = TRAILING_STOP_WORDS.replace(value, "").trim()
        value = value.replace(Regex("(?i)^(?:the|a|an)\\s+"), "").trim()
        if (value.length < 2 || !value.any { it.isLetter() }) return ""
        if (value.lowercase() in IGNORED_MERCHANTS) return ""
        if (value.contains("account", ignoreCase = true)) return ""
        return value.take(48).trim()
    }

    private fun extractAccount(text: String, sourcePackage: String): String {
        val last4 = LAST_FOUR.find(text)?.groupValues?.get(1)
        val bank = BANK_NAME.find(text)?.value?.let(::normalizeBank)
        return when {
            last4 != null && bank != null -> "$bank **** $last4"
            last4 != null -> "Account **** $last4"
            bank != null -> bank
            sourcePackage.isNotBlank() -> NotificationSources.labelFor(sourcePackage)
            else -> "Notification"
        }
    }

    private fun normalizeBank(raw: String): String = when (raw.lowercase()) {
        "hdfc" -> "HDFC"
        "icici" -> "ICICI"
        "sbi", "state bank", "state bank of india" -> "SBI"
        "axis" -> "Axis"
        "kotak" -> "Kotak"
        "yes bank" -> "Yes Bank"
        "indusind" -> "IndusInd"
        "idfc", "idfc first" -> "IDFC First"
        "bank of baroda" -> "Bank of Baroda"
        "canara" -> "Canara"
        "union bank" -> "Union Bank"
        "federal bank" -> "Federal Bank"
        "rbl" -> "RBL"
        "hsbc" -> "HSBC"
        "citi", "citibank" -> "Citi"
        "chase" -> "Chase"
        "revolut" -> "Revolut"
        "amex", "american express" -> "Amex"
        "dbs" -> "DBS"
        "au small finance" -> "AU Bank"
        else -> raw.replaceFirstChar { it.uppercase() }
    }

    private fun detectCurrency(matched: String): String {
        val value = matched.lowercase()
        return when {
            value.contains("₹") || value.contains("rs") || value.contains("inr") -> "INR"
            value.contains("€") || value.contains("eur") -> "EUR"
            value.contains("£") || value.contains("gbp") -> "GBP"
            value.contains("$") || value.contains("usd") -> "USD"
            else -> "USD"
        }
    }

    private fun detectCycle(lowerText: String): String = when {
        YEARLY_WORDS.containsMatchIn(lowerText) -> "YEARLY"
        QUARTERLY_WORDS.containsMatchIn(lowerText) -> "QUARTERLY"
        WEEKLY_WORDS.containsMatchIn(lowerText) -> "WEEKLY"
        else -> "MONTHLY"
    }

    private fun classify(text: String, merchant: String): String {
        val haystack = "$text $merchant".lowercase()
        for ((category, keywords) in CATEGORY_KEYWORDS) {
            if (keywords.any { haystack.contains(it) }) return category
        }
        return "Other"
    }

    companion object {
        private const val MAX_TEXT_LENGTH = 500
        private const val MAX_AMOUNT = 100_000_000.0

        private val WHITESPACE = Regex("\\s+")

        private val DEBIT_WORDS = Regex(
            "(?i)\\b(debited|debit|paid|spent|purchase[ds]?|charged|withdrawn|sent|deducted|renewed|renewal|txn of|payment of)\\b"
        )
        private val CREDIT_WORDS = Regex(
            "(?i)\\b(credited|credit|received|refund(?:ed)?|cashback|deposited|reversal)\\b"
        )
        private val AUTOPAY_WORDS = Regex(
            "(?i)\\b(autopay|auto-pay|auto pay|e-?mandate|mandate|recurring|subscription|renewal|renewed|standing instruction)\\b"
        )

        private val CURRENCY_FIRST = Regex(
            "(?i)(?:₹|rs\\.?|inr|usd|eur|gbp|£|€|\\$)\\s*([0-9][0-9,]*(?:\\.[0-9]{1,2})?)"
        )
        private val AMOUNT_FIRST = Regex(
            "(?i)([0-9][0-9,]*(?:\\.[0-9]{1,2})?)\\s*(?:₹|rs\\.?|inr|usd|eur|gbp|£|€|dollars?)"
        )

        private const val MERCHANT_BODY = "([A-Za-z0-9][A-Za-z0-9@&._'\\- ]{1,39}?)"
        private const val MERCHANT_END =
            "(?=\\s+(?:via|using|from|on|for|ref|refno|upi|txn|id|ending|card|a/c|ac|subscription|renewal|autopay|mandate)\\b|[.,;|!]|\\z)"

        private val DEBIT_MERCHANT_PATTERNS = listOf(
            Regex("(?i)\\bto\\s+$MERCHANT_BODY$MERCHANT_END"),
            Regex("(?i)\\bat\\s+$MERCHANT_BODY$MERCHANT_END"),
            Regex("(?i)\\bfor\\s+$MERCHANT_BODY$MERCHANT_END"),
            Regex("(?i)\\bfrom\\s+$MERCHANT_BODY$MERCHANT_END")
        )
        private val CREDIT_MERCHANT_PATTERNS = listOf(
            Regex("(?i)\\bfrom\\s+$MERCHANT_BODY$MERCHANT_END"),
            Regex("(?i)\\bto\\s+$MERCHANT_BODY$MERCHANT_END"),
            Regex("(?i)\\bat\\s+$MERCHANT_BODY$MERCHANT_END"),
            Regex("(?i)\\bfor\\s+$MERCHANT_BODY$MERCHANT_END")
        )

        private val TRAILING_STOP_WORDS = Regex(
            "(?i)\\s+(?:on|via|using|through|from|at|for|ref|refno|upi|txn|id|card|a/c|ac|ending)$"
        )

        private val IGNORED_MERCHANTS = setOf(
            "a/c", "ac", "account", "your account", "card", "credit card", "debit card",
            "bank account", "upi", "autopay", "to", "from", "for", "unknown"
        )

        private val LAST_FOUR = Regex(
            "(?i)(?:ending(?:\\s+(?:in|with))?|x{2,}|\\*{2,}|\\.{2,})\\s*(\\d{4})\\b"
        )
        private val BANK_NAME = Regex(
            "(?i)\\b(hdfc|icici|sbi|state bank of india|state bank|axis|kotak|yes bank|indusind|idfc first|idfc|bank of baroda|canara|union bank|federal bank|rbl|hsbc|citi|citibank|chase|revolut|american express|amex|dbs|au small finance)\\b"
        )

        private val YEARLY_WORDS = Regex("(?i)\\b(yearly|annual(?:ly)?|per year|every year|/yr)\\b")
        private val QUARTERLY_WORDS = Regex("(?i)\\b(quarterly|per quarter|every 3 months|every three months)\\b")
        private val WEEKLY_WORDS = Regex("(?i)\\b(weekly|per week|every week)\\b")

        private val CATEGORY_KEYWORDS: List<Pair<String, List<String>>> = listOf(
            "Entertainment" to listOf(
                "netflix", "spotify", "youtube", "prime", "hotstar", "disney", "bookmyshow",
                "cinema", "game", "gaming", "music", "streaming"
            ),
            "Food" to listOf(
                "swiggy", "zomato", "restaurant", "cafe", "coffee", "food", "pizza", "burger",
                "dominos", "mcdonald", "starbucks", "grocery", "bigbasket", "blinkit", "zepto",
                "instamart", "bakery"
            ),
            "Travel" to listOf(
                "uber", "ola", "rapido", "irctc", "flight", "hotel", "makemytrip", "goibibo",
                "metro", "fuel", "petrol", "indigo", "airlines", "cab", "train"
            ),
            "Bills" to listOf(
                "electricity", "water bill", "gas bill", "broadband", "airtel", "jio",
                "vodafone", " recharge", "insurance", "rent", "utility", "postpaid"
            ),
            "Health" to listOf(
                "pharmacy", "hospital", "clinic", "apollo", "medplus", "doctor", "health",
                "medicine", "dental"
            ),
            "Subscription" to listOf("subscription", "renewal", "membership", "annual plan"),
            "Shopping" to listOf(
                "amazon", "flipkart", "myntra", "ajio", "meesho", "shop", "store", "mall",
                "nykaa", "decathlon", "ikea"
            )
        )
    }
}
