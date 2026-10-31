package com.expensetracker.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "autopays")
data class AutopayEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val serviceName: String,
    val estimatedAmount: Double,
    val billingCycle: String,
    val lastChargedTimestamp: Long,
    val nextExpectedTimestamp: Long,
    val status: String,
    val category: String
)
