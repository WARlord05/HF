package com.expensetracker.app.parser

import java.time.Instant
import java.time.ZoneId

object BillingCycle {
    fun next(fromMillis: Long, cycle: String): Long {
        val time = Instant.ofEpochMilli(fromMillis).atZone(ZoneId.systemDefault())
        val next = when (cycle) {
            "WEEKLY" -> time.plusWeeks(1)
            "QUARTERLY" -> time.plusMonths(3)
            "YEARLY" -> time.plusYears(1)
            else -> time.plusMonths(1)
        }
        return next.toInstant().toEpochMilli()
    }

    fun monthlyEquivalent(amount: Double, cycle: String): Double = when (cycle) {
        "WEEKLY" -> amount * 52 / 12
        "QUARTERLY" -> amount / 3
        "YEARLY" -> amount / 12
        else -> amount
    }

    fun label(cycle: String): String = when (cycle) {
        "WEEKLY" -> "Weekly"
        "QUARTERLY" -> "Quarterly"
        "YEARLY" -> "Yearly"
        else -> "Monthly"
    }
}
