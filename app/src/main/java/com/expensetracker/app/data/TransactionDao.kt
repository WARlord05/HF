package com.expensetracker.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE autopayId = :autopayId ORDER BY timestamp DESC")
    fun observeForAutopay(autopayId: Long): Flow<List<TransactionEntity>>

    @Query(
        "SELECT * FROM transactions WHERE merchantName = :merchant AND amount = :amount " +
            "AND timestamp BETWEEN :since AND :until LIMIT 1"
    )
    suspend fun findRecent(merchant: String, amount: Double, since: Long, until: Long): TransactionEntity?

    @Query("UPDATE transactions SET autopayId = NULL WHERE autopayId = :autopayId")
    suspend fun clearAutopayLink(autopayId: Long)

    @Insert
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}
