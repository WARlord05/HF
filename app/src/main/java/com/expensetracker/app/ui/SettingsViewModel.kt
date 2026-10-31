package com.expensetracker.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.app.data.ExpenseRepository
import com.expensetracker.app.data.llm.LlmFallbackParser
import com.expensetracker.app.data.llm.LlmModelManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: ExpenseRepository,
    modelManager: LlmModelManager,
    llmParser: LlmFallbackParser
) : ViewModel() {
    val modelState = modelManager.state
    val aiRunning = llmParser.running

    fun clearAll() = viewModelScope.launch {
        repository.clearAll()
    }
}
