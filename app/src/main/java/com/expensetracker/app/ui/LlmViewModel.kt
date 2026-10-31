package com.expensetracker.app.ui

import androidx.lifecycle.ViewModel
import com.expensetracker.app.data.llm.LlmModelManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LlmViewModel @Inject constructor(
    private val modelManager: LlmModelManager
) : ViewModel() {
    val state = modelManager.state

    fun download() = modelManager.download()

    fun delete() = modelManager.deleteModel()
}
