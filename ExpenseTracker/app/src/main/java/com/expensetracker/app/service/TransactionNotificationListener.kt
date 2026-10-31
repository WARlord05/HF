package com.expensetracker.app.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.widget.Toast
import com.expensetracker.app.data.ExpenseRepository
import com.expensetracker.app.parser.NotificationSources
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class TransactionNotificationListener : NotificationListenerService() {

    @Inject
    lateinit var repository: ExpenseRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val notification = sbn.notification ?: return
        if (sbn.packageName == packageName) return
        if (sbn.isOngoing) return
        if (notification.flags and Notification.FLAG_GROUP_SUMMARY != 0) return
        if (!NotificationSources.isTarget(sbn.packageName)) return

        val extras = notification.extras
        val text = listOf(
            extras.getCharSequence(Notification.EXTRA_TITLE),
            extras.getCharSequence(Notification.EXTRA_TEXT),
            extras.getCharSequence(Notification.EXTRA_BIG_TEXT),
            extras.getCharSequence(Notification.EXTRA_SUB_TEXT)
        ).filterNotNull().joinToString(" ").trim()

        if (text.isBlank()) return

        scope.launch {
            val parsed = repository.recordNotification(
                sourcePackage = sbn.packageName,
                text = text,
                postedAt = sbn.postTime.takeIf { it > 0 } ?: System.currentTimeMillis()
            ) ?: return@launch

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@TransactionNotificationListener,
                    "Tracked ${parsed.currency} ${parsed.amount} · ${parsed.merchantName}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
