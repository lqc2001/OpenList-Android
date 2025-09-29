package com.openlist.android.data.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import android.util.Base64
import java.security.KeyStore
import java.security.SecureRandom

class SecurityHelper(context: Context) {

    private val masterKeyAlias = "_alist_master_key_"
    private val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
    private val masterKey: MasterKey

    init {
        val masterKeyBuilder = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)

        masterKey = masterKeyBuilder.build()
    }

    fun getEncryptedSharedPreferences(context: Context): EncryptedSharedPreferences {
        return EncryptedSharedPreferences.create(
            context,
            "alist_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as EncryptedSharedPreferences
    }

    fun encryptData(data: String): String {
        try {
            val cipher = getCipher()
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())

            val iv = ByteArray(12)
            SecureRandom().nextBytes(iv)

            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey(), GCMParameterSpec(128, iv))

            val encryptedData = cipher.doFinal(data.toByteArray())

            // Combine IV and encrypted data
            val combined = ByteArray(iv.size + encryptedData.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(encryptedData, 0, combined, iv.size, encryptedData.size)

            return Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            throw SecurityException("Encryption failed", e)
        }
    }

    fun decryptData(encryptedData: String): String {
        try {
            val combined = Base64.decode(encryptedData, Base64.NO_WRAP)

            if (combined.size < 12) {
                throw IllegalArgumentException("Invalid encrypted data")
            }

            val iv = ByteArray(12)
            val data = ByteArray(combined.size - 12)

            System.arraycopy(combined, 0, iv, 0, 12)
            System.arraycopy(combined, 12, data, 0, data.size)

            val cipher = getCipher()
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(), GCMParameterSpec(128, iv))

            val decryptedData = cipher.doFinal(data)
            return String(decryptedData)
        } catch (e: Exception) {
            throw SecurityException("Decryption failed", e)
        }
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyAlias = "alist_data_encryption_key"

        if (!keyStore.containsAlias(keyAlias)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )

            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()

            keyGenerator.init(keyGenParameterSpec)
            return keyGenerator.generateKey()
        }

        val secretKeyEntry = keyStore.getEntry(keyAlias, null) as? KeyStore.SecretKeyEntry
            ?: throw IllegalStateException("Failed to retrieve secret key")

        return secretKeyEntry.secretKey
    }

    private fun getCipher(): Cipher {
        return Cipher.getInstance(TRANSFORMATION)
    }

    fun clearSensitiveData() {
        try {
            // Clear the secret key from keystore
            val keyAlias = "alist_data_encryption_key"
            if (keyStore.containsAlias(keyAlias)) {
                keyStore.deleteEntry(keyAlias)
            }
        } catch (e: Exception) {
            // Log error but don't throw to avoid crashing the app
            e.printStackTrace()
        }
    }

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"

        @Volatile
        private var instance: SecurityHelper? = null

        fun getInstance(context: Context): SecurityHelper {
            return instance ?: synchronized(this) {
                instance ?: SecurityHelper(context.applicationContext).also { instance = it }
            }
        }
    }
}