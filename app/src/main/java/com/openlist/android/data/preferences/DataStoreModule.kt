package com.openlist.android.data.preferences

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.openlist.android.data.security.SecurityHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import android.util.Base64

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "alist_preferences")

object DataStoreModule {

    fun provideDataStore(
        context: Context
    ): DataStore<Preferences> {
        return context.dataStore
    }

    fun providePreferencesRepository(
        dataStore: DataStore<Preferences>,
        context: Context
    ): PreferencesRepository {
        return PreferencesRepositoryImpl(dataStore, context)
    }
}

interface PreferencesRepository {
    suspend fun saveAuthToken(token: String)
    suspend fun saveServerUrl(url: String)
    suspend fun saveUsername(username: String)
    suspend fun savePassword(password: String)
    suspend fun saveRememberMe(remember: Boolean)

    fun getAuthToken(): Flow<String?>
    fun getServerUrl(): Flow<String?>
    fun getUsername(): Flow<String?>
    fun getPassword(): Flow<String?>
    fun getRememberMe(): Flow<Boolean>

    suspend fun clearAuthData()
}

class PreferencesRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
    private val context: Context
) : PreferencesRepository {

    private val securityHelper = SecurityHelper.getInstance(context)

    companion object {
        private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")
        private val SERVER_URL_KEY = stringPreferencesKey("server_url")
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val PASSWORD_KEY = stringPreferencesKey("password")
        private val REMEMBER_ME_KEY = booleanPreferencesKey("remember_me")
    }

    override suspend fun saveAuthToken(token: String) {
        dataStore.edit { preferences ->
            preferences[AUTH_TOKEN_KEY] = securityHelper.encryptData(token)
        }
    }

    override suspend fun saveServerUrl(url: String) {
        dataStore.edit { preferences ->
            preferences[SERVER_URL_KEY] = url
        }
    }

    override suspend fun saveUsername(username: String) {
        dataStore.edit { preferences ->
            preferences[USERNAME_KEY] = username
        }
    }

    override suspend fun savePassword(password: String) {
        dataStore.edit { preferences ->
            preferences[PASSWORD_KEY] = if (password.isEmpty()) "" else securityHelper.encryptData(password)
        }
    }

    override suspend fun saveRememberMe(remember: Boolean) {
        dataStore.edit { preferences ->
            preferences[REMEMBER_ME_KEY] = remember
        }
    }

    override fun getAuthToken(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[AUTH_TOKEN_KEY]?.let { encryptedToken ->
                try {
                    securityHelper.decryptData(encryptedToken)
                } catch (e: Exception) {
                    null
                }
            }
        }
    }

    override fun getServerUrl(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[SERVER_URL_KEY]
        }
    }

    override fun getUsername(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[USERNAME_KEY]
        }
    }

    override fun getPassword(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[PASSWORD_KEY]?.let { encryptedPassword ->
                if (encryptedPassword.isEmpty()) {
                    ""
                } else {
                    try {
                        securityHelper.decryptData(encryptedPassword)
                    } catch (e: Exception) {
                        ""
                    }
                }
            }
        }
    }

    override fun getRememberMe(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[REMEMBER_ME_KEY] ?: false
        }
    }

    override suspend fun clearAuthData() {
        dataStore.edit { preferences ->
            preferences.remove(AUTH_TOKEN_KEY)
            preferences.remove(USERNAME_KEY)
            preferences.remove(PASSWORD_KEY)
            preferences.remove(REMEMBER_ME_KEY)
        }
    }
}