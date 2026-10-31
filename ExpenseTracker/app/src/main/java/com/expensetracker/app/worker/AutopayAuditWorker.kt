package com.expensetracker.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.expensetracker.app.data.ExpenseRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class AutopayAuditWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repository: ExpenseRepository
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        repository.markOverdueAutopays()
        return Result.success()
    }
}
