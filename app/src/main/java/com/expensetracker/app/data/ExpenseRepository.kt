package com.expensetracker.app.data

import com.expensetracker.app.data.llm.LlmFallbackParser
import com.expensetracker.app.parser.BillingCycle
import com.expensetracker.app.parser.NotificationParser
import com.expensetracker.app.parser.ParsedNotification
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepository @Inject constructor(
    private val transactionDao: TransactionDao,
    private val autopayDao: AutopayDao,
    private val parser: NotificationParser,
    private val llmParser: LlmFallbackParser
) {
    val transactions: Flow<List<TransactionEntity>> = transactionDao.observeAll()
    val autopays: Flow<List<AutopayEntity>> = autopayDao.observeAll()

    fun historyFor(autopayId: Long): Flow<List<TransactionEntity>> =
        transactionDao.observeForAutopay(autopayId)

    suspend fun save(transaction: TransactionEntity) {
        if (transaction.id == 0L) transactionDao.insert(transaction) else transactionDao.update(transaction)
    }

    suspend fun delete(transaction: TransactionEntity) = transactionDao.delete(transaction)

    suspend fun setAutopayStatus(autopay: AutopayEntity, status: String) =
        autopayDao.update(autopay.copy(status = status))

    suspend fun reactivate(autopay: AutopayEntity) =
        autopayDao.update(
            autopay.copy(
                status = "ACTIVE",
                nextExpectedTimestamp = BillingCycle.next(
                    autopay.lastChargedTimestamp,
                    autopay.billingCycle
                )
            )
        )

    suspend fun markOverdueAutopays(now: Long = System.currentTimeMillis()) =
        autopayDao.markStale(now - STALE_GRACE_MS)

    suspend fun recordNotification(sourcePackage: String, text: String, postedAt: Long): ParsedNotification? {
        val parsed = parser.parse(text, postedAt, sourcePackage)
            ?: parser.extractAmount(text)?.let { amount ->
                llmParser.parse(text, amount, postedAt, sourcePackage)
            }
            ?: return null
        if (transactionDao.findRecent(
                merchant = parsed.merchantName,
                amount = parsed.amount,
                since = postedAt - DEDUPE_WINDOW_MS,
                until = postedAt + DEDUPE_WINDOW_MS
            ) != null
        ) {
            return null
        }
        val autopayId = if (parsed.isAutopay) upsertAutopay(parsed) else null
        transactionDao.insert(parsed.toEntity(autopayId, text))
        return parsed
    }

    private suspend fun upsertAutopay(parsed: ParsedNotification): Long {
        val nextExpected = BillingCycle.next(parsed.timestamp, parsed.billingCycle)
        val existing = autopayDao.findByName(parsed.merchantName)
        return if (existing == null) {
            autopayDao.insert(
                AutopayEntity(
                    serviceName = parsed.merchantName,
                    estimatedAmount = parsed.amount,
                    billingCycle = parsed.billingCycle,
                    lastChargedTimestamp = parsed.timestamp,
                    nextExpectedTimestamp = nextExpected,
                    status = "ACTIVE",
                    category = parsed.category
                )
            )
        } else {
            autopayDao.update(
                existing.copy(
                    estimatedAmount = parsed.amount,
                    billingCycle = parsed.billingCycle,
                    lastChargedTimestamp = parsed.timestamp,
                    nextExpectedTimestamp = nextExpected,
                    status = "ACTIVE",
                    category = parsed.category
                )
            )
            existing.id
        }
    }

    companion object {
        private const val DEDUPE_WINDOW_MS = 60_000L
        private const val STALE_GRACE_MS = 7L * 24 * 60 * 60 * 1000
    }
}

fun displayStatus(autopay: AutopayEntity, now: Long = System.currentTimeMillis()): String = when {
    autopay.status == "CANCELLED" -> "CANCELLED"
    autopay.status == "PAUSED" -> "PAUSED"
    autopay.status == "STALE" -> "STALE"
    autopay.nextExpectedTimestamp < now -> "STALE"
    autopay.nextExpectedTimestamp - now <= UPCOMING_WINDOW_MS -> "UPCOMING"
    else -> "ACTIVE"
}

private const val UPCOMING_WINDOW_MS = 7L * 24 * 60 * 60 * 1000
