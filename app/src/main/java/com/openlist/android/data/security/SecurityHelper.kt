package com.openlist.android.data.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecurityHelper private constructor(private val context: Context) {

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val encryptedPrefs: EncryptedSharedPreferences by lazy {
        createEncryptedSharedPreferences()
    }

    companion object {
        private const val TAG = "SecurityHelper"
        private const val PREFS_NAME = "alist_secure_prefs"

        @Volatile
        private var instance: SecurityHelper? = null

        fun getInstance(context: Context): SecurityHelper {
            return instance ?: synchronized(this) {
                instance ?: SecurityHelper(context.applicationContext ?: throw IllegalArgumentException("Context cannot be null"))
                    .also { instance = it }
            }
        }
    }

    private fun createEncryptedSharedPreferences(): EncryptedSharedPreferences {
        return try {
            Log.d(TAG, "Creating encrypted shared preferences")
            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            ) as EncryptedSharedPreferences
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create encrypted shared preferences", e)
            throw e
        }
    }

    fun saveString(key: String, value: String?): Boolean {
        return try {
            Log.d(TAG, "Saving string for key: $key")
            if (value == null) {
                encryptedPrefs.edit().remove(key).apply()
                Log.d(TAG, "Removed key: $key")
                true
            } else {
                encryptedPrefs.edit().putString(key, value).apply()
                Log.d(TAG, "Saved string for key: $key, length: ${value.length}")
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save string for key: $key", e)
            false
        }
    }

    fun getString(key: String, defaultValue: String? = null): String? {
        return try {
            Log.d(TAG, "Getting string for key: $key")
            val value = encryptedPrefs.getString(key, defaultValue)
            Log.d(TAG, "Retrieved string for key: $key, value exists: ${value != null}")
            value
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get string for key: $key", e)
            defaultValue
        }
    }

    fun saveBoolean(key: String, value: Boolean): Boolean {
        return try {
            Log.d(TAG, "Saving boolean for key: $key, value: $value")
            encryptedPrefs.edit().putBoolean(key, value).apply()
            Log.d(TAG, "Saved boolean for key: $key")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save boolean for key: $key", e)
            false
        }
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return try {
            Log.d(TAG, "Getting boolean for key: $key")
            val value = encryptedPrefs.getBoolean(key, defaultValue)
            Log.d(TAG, "Retrieved boolean for key: $key, value: $value")
            value
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get boolean for key: $key", e)
            defaultValue
        }
    }

    fun remove(key: String): Boolean {
        return try {
            Log.d(TAG, "Removing key: $key")
            encryptedPrefs.edit().remove(key).apply()
            Log.d(TAG, "Removed key: $key")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remove key: $key", e)
            false
        }
    }

    fun clearAll(): Boolean {
        return try {
            Log.d(TAG, "Clearing all encrypted preferences")
            encryptedPrefs.edit().clear().apply()
            Log.d(TAG, "Cleared all encrypted preferences")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear all encrypted preferences", e)
            false
        }
    }

    fun contains(key: String): Boolean {
        return try {
            Log.d(TAG, "Checking if key exists: $key")
            val exists = encryptedPrefs.contains(key)
            Log.d(TAG, "Key exists: $key, exists: $exists")
            exists
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check if key exists: $key", e)
            false
        }
    }

    fun getAllPreferences(): Map<String, *> {
        return try {
            Log.d(TAG, "Getting all preferences")
            val allPrefs = encryptedPrefs.all
            Log.d(TAG, "Retrieved ${allPrefs.size} preferences")
            allPrefs
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get all preferences", e)
            emptyMap<String, Any>()
        }
    }
}