package com.expensetracker.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.expensetracker.app.worker.AutopayAuditWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class ExpenseTrackerApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        val request = PeriodicWorkRequestBuilder<AutopayAuditWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork("autopay-audit", ExistingPeriodicWorkPolicy.KEEP, request)
    }
}
