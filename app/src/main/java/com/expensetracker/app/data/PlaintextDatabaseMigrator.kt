package com.expensetracker.app.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import java.io.File
import java.io.FileInputStream

/**
 * One-time import of the pre-SQLCipher plaintext database into the encrypted database.
 * The plaintext file is renamed aside before Room creates the encrypted file, then rows are
 * copied in the Room open callback and the plaintext backup is deleted.
 */
class PlaintextDatabaseMigrator(private val context: Context) : RoomDatabase.Callback() {

    override fun onOpen(db: SupportSQLiteDatabase) {
        copyIntoEncrypted(db)
    }

    fun prepare(): File? {
        val main = context.getDatabasePath(DB_NAME)
        val backup = backupFile(main)
        if (backup.exists()) return backup
        if (!main.exists() || !isPlaintext(main)) return null

        val moved = mutableListOf<String>()
        for (suffix in SIDECARS) {
            val source = File(main.path + suffix)
            if (!source.exists()) continue
            val destination = File(backup.path + suffix)
            destination.delete()
            if (!source.renameTo(destination)) {
                restoreSidecars(main, backup, moved)
                return null
            }
            moved.add(suffix)
        }
        if (!main.renameTo(backup)) {
            restoreSidecars(main, backup, moved)
            return null
        }
        return backup
    }

    private fun copyIntoEncrypted(encrypted: SupportSQLiteDatabase) {
        val backup = backupFile(context.getDatabasePath(DB_NAME))
        if (!backup.exists()) return

        val plain = SQLiteDatabase.openDatabase(backup.path, null, SQLiteDatabase.OPEN_READWRITE)
        try {
            encrypted.beginTransaction()
            try {
                copyAutopays(plain, encrypted)
                copyTransactions(plain, encrypted)
                encrypted.setTransactionSuccessful()
            } finally {
                encrypted.endTransaction()
            }
        } finally {
            plain.close()
        }
        backup.delete()
        File(backup.path + "-wal").delete()
        File(backup.path + "-shm").delete()
        File(backup.path + "-journal").delete()
    }

    private fun copyAutopays(plain: SQLiteDatabase, encrypted: SupportSQLiteDatabase) {
        plain.rawQuery(
            "SELECT id, serviceName, estimatedAmount, billingCycle, lastChargedTimestamp, " +
                "nextExpectedTimestamp, status, category FROM autopays",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                encrypted.execSQL(
                    "INSERT OR IGNORE INTO autopays (id, serviceName, estimatedAmount, billingCycle, " +
                        "lastChargedTimestamp, nextExpectedTimestamp, status, category) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    arrayOf(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getDouble(2),
                        cursor.getString(3),
                        cursor.getLong(4),
                        cursor.getLong(5),
                        cursor.getString(6),
                        cursor.getString(7)
                    )
                )
            }
        }
    }

    private fun copyTransactions(plain: SQLiteDatabase, encrypted: SupportSQLiteDatabase) {
        plain.rawQuery(
            "SELECT id, merchantName, amount, currency, timestamp, category, accountSource, " +
                "transactionType, isAutopay, autopayId, rawNotificationText FROM transactions",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                encrypted.execSQL(
                    "INSERT OR IGNORE INTO transactions (id, merchantName, amount, currency, timestamp, " +
                        "category, accountSource, transactionType, isAutopay, autopayId, rawNotificationText) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    arrayOf(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getDouble(2),
                        cursor.getString(3),
                        cursor.getLong(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getString(7),
                        cursor.getInt(8),
                        if (cursor.isNull(9)) null else cursor.getLong(9),
                        if (cursor.isNull(10)) null else cursor.getString(10)
                    )
                )
            }
        }
    }

    companion object {
        private const val DB_NAME = "expense-tracker.db"
        private const val PLAINTEXT_HEADER = "SQLite format 3\u0000"
        private val SIDECARS = listOf("-wal", "-shm", "-journal")

        private fun backupFile(main: File) = File(main.path + ".plain.bak")

        private fun restoreSidecars(main: File, backup: File, suffixes: List<String>) {
            suffixes.forEach { suffix ->
                File(backup.path + suffix).renameTo(File(main.path + suffix))
            }
        }

        private fun isPlaintext(file: File): Boolean {
            val header = ByteArray(PLAINTEXT_HEADER.length)
            FileInputStream(file).use { it.read(header) }
            return header.contentEquals(PLAINTEXT_HEADER.toByteArray(Charsets.US_ASCII))
        }
    }
}
