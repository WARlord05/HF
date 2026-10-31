package com.expensetracker.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.app.data.AutopayEntity
import com.expensetracker.app.data.ExpenseRepository
import com.expensetracker.app.data.TransactionEntity
import com.expensetracker.app.data.displayStatus
import com.expensetracker.app.parser.BillingCycle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AutopaysUiState(
    val autopays: List<AutopayEntity> = emptyList(),
    val monthlyTotal: Double = 0.0,
    val upcomingCount: Int = 0,
    val upcomingTotal: Double = 0.0,
    val currency: String = "USD"
)

@HiltViewModel
class AutopaysViewModel @Inject constructor(private val repository: ExpenseRepository) : ViewModel() {

    val uiState = combine(repository.autopays, repository.transactions) { autopays, transactions ->
        val now = System.currentTimeMillis()
        val active = autopays.filter { displayStatus(it, now) == "ACTIVE" || displayStatus(it, now) == "UPCOMING" }
        val upcoming = active.filter { it.nextExpectedTimestamp in now..(now + UPCOMING_WINDOW_MS) }
        AutopaysUiState(
            autopays = autopays,
            monthlyTotal = active.sumOf { BillingCycle.monthlyEquivalent(it.estimatedAmount, it.billingCycle) },
            upcomingCount = upcoming.size,
            upcomingTotal = upcoming.sumOf { it.estimatedAmount },
            currency = transactions.firstOrNull()?.currency ?: "USD"
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AutopaysUiState())

    fun historyFor(autopayId: Long): Flow<List<TransactionEntity>> = repository.historyFor(autopayId)

    fun cancel(autopay: AutopayEntity) = viewModelScope.launch {
        repository.setAutopayStatus(autopay, "CANCELLED")
    }

    fun reactivate(autopay: AutopayEntity) = viewModelScope.launch {
        repository.reactivate(autopay)
    }

    fun remove(autopay: AutopayEntity) = viewModelScope.launch {
        repository.removeAutopay(autopay)
    }

    private companion object {
        const val UPCOMING_WINDOW_MS = 7L * 24 * 60 * 60 * 1000
    }
}
