package com.expensetracker.app.ui

import android.content.ComponentName
import android.content.Context
import android.provider.Settings

object NotificationAccess {
    fun isGranted(context: Context): Boolean {
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        ) ?: return false
        return enabled.split(':').any {
            ComponentName.unflattenFromString(it)?.packageName == context.packageName
        }
    }
}
