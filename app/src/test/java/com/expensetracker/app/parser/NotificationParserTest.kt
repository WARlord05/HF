package com.expensetracker.app.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationParserTest {

    private val parser = NotificationParser()

    @Test
    fun parsesDebitWithMerchantAndAccount() {
        val parsed = parser.parse(
            "Rs. 450 debited from A/c ****1234 to Swiggy via UPI",
            postedAt = 1_700_000_000_000L
        )!!

        assertEquals("DEBIT", parsed.transactionType)
        assertEquals(450.0, parsed.amount, 0.001)
        assertEquals("INR", parsed.currency)
        assertEquals("Swiggy", parsed.merchantName)
        assertEquals("Account **** 1234", parsed.accountSource)
        assertEquals("Food", parsed.category)
        assertTrue(!parsed.isAutopay)
    }

    @Test
    fun parsesCreditWithBankAndMerchant() {
        val parsed = parser.parse(
            "Your account XX4091 has been credited with Rs 2,500.00 from Acme Corp",
            postedAt = 1_700_000_000_000L
        )!!

        assertEquals("CREDIT", parsed.transactionType)
        assertEquals(2500.0, parsed.amount, 0.001)
        assertEquals("Acme Corp", parsed.merchantName)
        assertEquals("Account **** 4091", parsed.accountSource)
    }

    @Test
    fun detectsCardDebitAndShoppingCategory() {
        val parsed = parser.parse(
            "Your HDFC Bank Credit Card ending 4091 has been debited with Rs 1,234.00 at Amazon on 12-05-2025",
            postedAt = 1_700_000_000_000L
        )!!

        assertEquals("HDFC **** 4091", parsed.accountSource)
        assertEquals("Amazon", parsed.merchantName)
        assertEquals("Shopping", parsed.category)
        assertEquals(1234.0, parsed.amount, 0.001)
    }

    @Test
    fun detectsMonthlyAutopay() {
        val parsed = parser.parse(
            "Autopay of Rs 649.00 executed for Netflix subscription renewal",
            postedAt = 1_700_000_000_000L
        )!!

        assertTrue(parsed.isAutopay)
        assertEquals("MONTHLY", parsed.billingCycle)
        assertEquals("Netflix", parsed.merchantName)
        assertEquals("Entertainment", parsed.category)
    }

    @Test
    fun detectsYearlyAutopay() {
        val parsed = parser.parse(
            "Annual subscription renewal of \$119.99 for Amazon Prime",
            postedAt = 1_700_000_000_000L
        )!!

        assertTrue(parsed.isAutopay)
        assertEquals("YEARLY", parsed.billingCycle)
        assertEquals("USD", parsed.currency)
        assertEquals(119.99, parsed.amount, 0.001)
        assertEquals("Amazon Prime", parsed.merchantName)
    }

    @Test
    fun parsesDisneyPlusAutopayMerchant() {
        val parsed = parser.parse(
            "Autopay of Rs 299.00 executed for Disney+ Hotstar subscription renewal",
            postedAt = 1_700_000_000_000L
        )!!

        assertTrue(parsed.isAutopay)
        assertEquals("Disney+ Hotstar", parsed.merchantName)
        assertEquals("Entertainment", parsed.category)
        assertEquals(299.0, parsed.amount, 0.001)
    }

    @Test
    fun ignoresNonTransactionNotifications() {
        assertNull(parser.parse("Your OTP is 123456", postedAt = 1_700_000_000_000L))
    }

    @Test
    fun rejectsOtpWithPaymentWording() {
        assertNull(
            parser.parse(
                "Use OTP 482913 to verify payment of Rs 500",
                postedAt = 1_700_000_000_000L
            )
        )
    }

    @Test
    fun paymentCueRejectsOrderUpdates() {
        assertTrue(parser.looksLikePayment("Txn alert: INR 1,299.00 done at Myntra"))
        assertTrue(parser.looksLikePayment("Rs 450 debited from A/c"))
        assertFalse(parser.looksLikePayment("Your order of INR 799 is out for delivery"))
        assertFalse(parser.looksLikePayment("Order confirmed for INR 999"))
    }

    @Test
    fun handlesVeryLongTextWithoutCrashing() {
        val longText = "Paid Rs 99 to TestStore via UPI " + "reminder ".repeat(500)
        val parsed = parser.parse(longText, postedAt = 1_700_000_000_000L)
        assertEquals("TestStore", parsed?.merchantName)
    }

    @Test
    fun extractsAmountEvenWithoutTransactionWords() {
        val amount = parser.extractAmount("Txn alert: INR 1,299.00 done at Myntra")!!
        assertEquals(1299.0, amount.amount, 0.001)
        assertEquals("INR", amount.currency)
    }

    @Test
    fun billingCycleComputesNextDate() {
        val start = 1_700_000_000_000L
        assertTrue(BillingCycle.next(start, "MONTHLY") > start)
        assertTrue(BillingCycle.next(start, "YEARLY") > BillingCycle.next(start, "MONTHLY"))
        assertEquals(100.0, BillingCycle.monthlyEquivalent(1200.0, "YEARLY"), 0.001)
    }
}
