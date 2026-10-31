package com.expensetracker.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AutopayDao {

    @Query("SELECT * FROM autopays ORDER BY nextExpectedTimestamp ASC")
    fun observeAll(): Flow<List<AutopayEntity>>

    @Query("SELECT * FROM autopays WHERE serviceName = :name COLLATE NOCASE AND status != 'CANCELLED' LIMIT 1")
    suspend fun findByName(name: String): AutopayEntity?

    @Insert
    suspend fun insert(autopay: AutopayEntity): Long

    @Update
    suspend fun update(autopay: AutopayEntity)

    @Query("UPDATE autopays SET status = 'STALE' WHERE status = 'ACTIVE' AND nextExpectedTimestamp < :before")
    suspend fun markStale(before: Long)

    @Delete
    suspend fun delete(autopay: AutopayEntity)

    @Query("DELETE FROM autopays")
    suspend fun deleteAll()
}
