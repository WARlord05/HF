package com.expensetracker.app.data

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages the SQLite database encryption passphrase using Android KeyStore
 * backed EncryptedSharedPreferences.
 */
@Singleton
class DatabaseKeyManager @Inject constructor(
    private val context: Context
) {

    /**
     * Retrieves the database passphrase byte array or generates a cryptographically
     * secure 256-bit passphrase if not present.
     */
    @Synchronized
    fun getOrCreatePassphrase(): ByteArray {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            PREFS_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        var encodedKey = sharedPreferences.getString(KEY_PASSPHRASE, null)
        if (encodedKey == null) {
            val randomBytes = ByteArray(32)
            SecureRandom().nextBytes(randomBytes)
            encodedKey = Base64.encodeToString(randomBytes, Base64.NO_WRAP)
            sharedPreferences.edit().putString(KEY_PASSPHRASE, encodedKey).apply()
            return randomBytes
        }

        return Base64.decode(encodedKey, Base64.NO_WRAP)
    }

    companion object {
        private const val PREFS_FILE_NAME = "secure_db_prefs"
        private const val KEY_PASSPHRASE = "db_encryption_passphrase"
    }
}
