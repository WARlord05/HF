package com.expensetracker.app.parser

/**
 * Only notifications posted by these packages are read. Packages are matched by exact name,
 * plus a small set of name heuristics for banking apps that are not listed.
 */
object NotificationSources {

    private val exactPackages = setOf(
        "com.google.android.apps.nbu.paisa.user",
        "com.phonepe.app",
        "net.one97.paytm",
        "com.google.android.apps.messaging",
        "com.android.mms",
        "com.samsung.android.messaging",
        "com.microsoft.android.smsorganizer",
        "com.revolut.revolut",
        "com.chase.sig.android",
        "com.chase",
        "com.americanexpress.android.acctsvcs.us",
        "com.cred",
        "com.dreamplug.androidapp"
    )

    private val packageHints = listOf(
        "bank", "paytm", "phonepe", "paisa", "wallet", "revolut", "chase", "amex",
        "cred", "messaging", "mms", "sms"
    )

    private val labels = mapOf(
        "com.google.android.apps.nbu.paisa.user" to "GPay",
        "com.phonepe.app" to "PhonePe",
        "net.one97.paytm" to "Paytm",
        "com.google.android.apps.messaging" to "SMS",
        "com.android.mms" to "SMS",
        "com.samsung.android.messaging" to "SMS",
        "com.microsoft.android.smsorganizer" to "SMS",
        "com.revolut.revolut" to "Revolut",
        "com.chase.sig.android" to "Chase",
        "com.americanexpress.android.acctsvcs.us" to "Amex",
        "com.dreamplug.androidapp" to "CRED"
    )

    fun isTarget(packageName: String): Boolean =
        packageName in exactPackages || packageHints.any { packageName.contains(it, ignoreCase = true) }

    fun labelFor(packageName: String): String =
        labels[packageName] ?: packageName.substringAfterLast('.').replaceFirstChar { it.uppercase() }
}
