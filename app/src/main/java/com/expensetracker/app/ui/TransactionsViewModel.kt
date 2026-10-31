package com.expensetracker.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.app.data.ExpenseRepository
import com.expensetracker.app.data.TransactionEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

enum class DateRange(val label: String) {
    ALL("All time"),
    THIS_MONTH("This month"),
    LAST_30("Last 30 days")
}

data class DayGroup(
    val date: LocalDate,
    val transactions: List<TransactionEntity>,
    val spent: Double
)

data class TransactionsUiState(
    val groups: List<DayGroup> = emptyList(),
    val categories: List<String> = emptyList(),
    val query: String = "",
    val category: String? = null,
    val range: DateRange = DateRange.ALL,
    val spent: Double = 0.0,
    val currency: String = "USD",
    val count: Int = 0
)

@HiltViewModel
class TransactionsViewModel @Inject constructor(private val repository: ExpenseRepository) : ViewModel() {

    private val query = MutableStateFlow("")
    private val category = MutableStateFlow<String?>(null)
    private val range = MutableStateFlow(DateRange.ALL)

    val uiState = combine(repository.transactions, query, category, range) { all, q, c, r ->
        val now = LocalDate.now()
        val filtered = all.filter { transaction ->
            val date = toDate(transaction.timestamp)
            val matchesQuery = q.isBlank() || listOf(
                transaction.merchantName,
                transaction.category,
                transaction.accountSource
            ).any { it.contains(q, ignoreCase = true) }
            val matchesCategory = c == null || transaction.category == c
            val matchesRange = when (r) {
                DateRange.ALL -> true
                DateRange.THIS_MONTH -> date.year == now.year && date.month == now.month
                DateRange.LAST_30 -> !date.isBefore(now.minusDays(30))
            }
            matchesQuery && matchesCategory && matchesRange
        }
        val groups = filtered
            .groupBy { toDate(it.timestamp) }
            .toSortedMap(compareByDescending { date: LocalDate -> date })
            .map { (date, items) ->
                DayGroup(
                    date = date,
                    transactions = items,
                    spent = items.filter { it.transactionType == "DEBIT" }.sumOf { it.amount }
                )
            }
        TransactionsUiState(
            groups = groups,
            categories = all.map { it.category }.distinct().sorted(),
            query = q,
            category = c,
            range = r,
            spent = filtered.filter { it.transactionType == "DEBIT" }.sumOf { it.amount },
            currency = all.firstOrNull()?.currency ?: "USD",
            count = filtered.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TransactionsUiState())

    fun setQuery(value: String) {
        query.value = value
    }

    fun setCategory(value: String?) {
        category.value = value
    }

    fun setRange(value: DateRange) {
        range.value = value
    }

    fun save(transaction: TransactionEntity) = viewModelScope.launch {
        repository.save(transaction)
    }

    fun delete(transaction: TransactionEntity) = viewModelScope.launch {
        repository.delete(transaction)
    }
}
