package com.openlist.android.data.preferences

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.openlist.android.data.security.SecurityHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.catch

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
    suspend fun clearCredentials()
    fun hasSavedCredentials(): Flow<Boolean>
}

class PreferencesRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
    private val context: Context
) : PreferencesRepository {

    private val securityHelper = SecurityHelper.getInstance(context)
    private val TAG = "PreferencesRepository"

    companion object {
        private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")
        private val SERVER_URL_KEY = stringPreferencesKey("server_url")
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val PASSWORD_KEY = stringPreferencesKey("password")
        private val REMEMBER_ME_KEY = booleanPreferencesKey("remember_me")
    }

    override suspend fun saveAuthToken(token: String) {
        try {
            Log.d(TAG, "Saving auth token")
            val success = securityHelper.saveString("auth_token", token)
            if (!success) {
                Log.e(TAG, "Failed to save auth token to encrypted storage")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving auth token", e)
        }
    }

    override suspend fun saveServerUrl(url: String) {
        try {
            Log.d(TAG, "Saving server URL: $url")
            dataStore.edit { preferences ->
                preferences[SERVER_URL_KEY] = url
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving server URL", e)
        }
    }

    override suspend fun saveUsername(username: String) {
        try {
            Log.d(TAG, "Saving username: $username")
            val success = securityHelper.saveString("username", username)
            if (!success) {
                Log.e(TAG, "Failed to save username to encrypted storage")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving username", e)
        }
    }

    override suspend fun savePassword(password: String) {
        try {
            Log.d(TAG, "Saving password")
            val success = securityHelper.saveString("password", password)
            if (!success) {
                Log.e(TAG, "Failed to save password to encrypted storage")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving password", e)
        }
    }

    override suspend fun saveRememberMe(remember: Boolean) {
        try {
            Log.d(TAG, "Saving remember me: $remember")
            val success = securityHelper.saveBoolean("remember_me", remember)
            if (!success) {
                Log.e(TAG, "Failed to save remember me to encrypted storage")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving remember me", e)
        }
    }

    override fun getAuthToken(): Flow<String?> {
        return dataStore.data.map { preferences ->
            try {
                securityHelper.getString("auth_token")
            } catch (e: Exception) {
                Log.e(TAG, "Error getting auth token", e)
                null
            }
        }.catch { e ->
            Log.e(TAG, "Flow error getting auth token", e)
            emit(null)
        }
    }

    override fun getServerUrl(): Flow<String?> {
        return dataStore.data.map { preferences ->
            try {
                preferences[SERVER_URL_KEY]
            } catch (e: Exception) {
                Log.e(TAG, "Error getting server URL", e)
                null
            }
        }.catch { e ->
            Log.e(TAG, "Flow error getting server URL", e)
            emit(null)
        }
    }

    override fun getUsername(): Flow<String?> {
        return dataStore.data.map { preferences ->
            try {
                securityHelper.getString("username")
            } catch (e: Exception) {
                Log.e(TAG, "Error getting username", e)
                null
            }
        }.catch { e ->
            Log.e(TAG, "Flow error getting username", e)
            emit(null)
        }
    }

    override fun getPassword(): Flow<String?> {
        return dataStore.data.map { preferences ->
            try {
                securityHelper.getString("password")
            } catch (e: Exception) {
                Log.e(TAG, "Error getting password", e)
                null
            }
        }.catch { e ->
            Log.e(TAG, "Flow error getting password", e)
            emit(null)
        }
    }

    override fun getRememberMe(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            try {
                securityHelper.getBoolean("remember_me", false)
            } catch (e: Exception) {
                Log.e(TAG, "Error getting remember me", e)
                false
            }
        }.catch { e ->
            Log.e(TAG, "Flow error getting remember me", e)
            emit(false)
        }
    }

    override suspend fun clearAuthData() {
        try {
            Log.d(TAG, "Clearing auth data")
            dataStore.edit { preferences ->
                preferences.remove(AUTH_TOKEN_KEY)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing auth data", e)
        }
    }

    override suspend fun clearCredentials() {
        try {
            Log.d(TAG, "Clearing all credentials")
            securityHelper.remove("username")
            securityHelper.remove("password")
            securityHelper.remove("remember_me")
            dataStore.edit { preferences ->
                preferences.remove(SERVER_URL_KEY)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing credentials", e)
        }
    }

    override fun hasSavedCredentials(): Flow<Boolean> {
        return kotlinx.coroutines.flow.flow {
            try {
                val rememberMe = securityHelper.getBoolean("remember_me", false)
                val username = securityHelper.getString("username")
                val password = securityHelper.getString("password")
                val hasCredentials = rememberMe && !username.isNullOrEmpty() && !password.isNullOrEmpty()
                Log.d(TAG, "Has saved credentials: $hasCredentials")
                emit(hasCredentials)
            } catch (e: Exception) {
                Log.e(TAG, "Error checking saved credentials", e)
                emit(false)
            }
        }.catch { e ->
            Log.e(TAG, "Flow error checking saved credentials", e)
            emit(false)
        }
    }
}