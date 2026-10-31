package com.expensetracker.app.data.llm

import com.expensetracker.app.parser.AmountMatch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LlmResponseParserTest {

    private val parser = LlmResponseParser()
    private val amount = AmountMatch(1299.0, "INR", 0..7)

    @Test
    fun mapsValidDebitAndKeepsRegexAmount() {
        val parsed = parser.parse(
            response = """{"is_transaction":true,"merchant":"Myntra","amount":999,"category":"Shopping","type":"DEBIT","autopay":false,"cycle":"MONTHLY"}""",
            amount = amount,
            postedAt = 123L,
            accountSource = "HDFC **** 1234"
        )!!

        assertEquals("Myntra", parsed.merchantName)
        assertEquals(1299.0, parsed.amount, 0.001)
        assertEquals("INR", parsed.currency)
        assertEquals("Shopping", parsed.category)
        assertEquals("DEBIT", parsed.transactionType)
        assertEquals("HDFC **** 1234", parsed.accountSource)
        assertEquals(123L, parsed.timestamp)
        assertFalse(parsed.isAutopay)
    }

    @Test
    fun rejectsNonTransaction() {
        assertNull(
            parser.parse(
                """{"is_transaction":false,"merchant":"Amazon","type":"DEBIT"}""",
                amount,
                1L,
                "SMS"
            )
        )
    }

    @Test
    fun rejectsMalformedOrMissingType() {
        assertNull(parser.parse("not json", amount, 1L, "SMS"))
        assertNull(
            parser.parse(
                """{"is_transaction":true,"merchant":"Shop","type":"NONE"}""",
                amount,
                1L,
                "SMS"
            )
        )
    }

    @Test
    fun parsesFencedJsonAndFallsBackToOtherCategory() {
        val parsed = parser.parse(
            response = "```json\n{\"is_transaction\": \"true\", \"merchant\": \"Local Store\", \"category\": \"Groceries\", \"type\": \"debit\"}\n```",
            amount = amount,
            postedAt = 1L,
            accountSource = "SMS"
        )!!

        assertEquals("Local Store", parsed.merchantName)
        assertEquals("Other", parsed.category)
        assertEquals("DEBIT", parsed.transactionType)
    }

    @Test
    fun autopayIsDebitOnlyAndValidated() {
        val debit = parser.parse(
            """{"is_transaction":true,"merchant":"Netflix","category":"Entertainment","type":"DEBIT","autopay":true,"cycle":"yearly"}""",
            amount,
            1L,
            "SMS"
        )!!
        assertTrue(debit.isAutopay)
        assertEquals("YEARLY", debit.billingCycle)

        val credit = parser.parse(
            """{"is_transaction":true,"merchant":"Employer","category":"Other","type":"CREDIT","autopay":true,"cycle":"YEARLY"}""",
            amount,
            1L,
            "SMS"
        )!!
        assertFalse(credit.isAutopay)
        assertEquals("MONTHLY", credit.billingCycle)
    }

    @Test
    fun blankMerchantBecomesUnknown() {
        val parsed = parser.parse(
            """{"is_transaction":true,"merchant":"  ","type":"DEBIT"}""",
            amount,
            1L,
            "SMS"
        )!!
        assertEquals("Unknown", parsed.merchantName)
    }
}
