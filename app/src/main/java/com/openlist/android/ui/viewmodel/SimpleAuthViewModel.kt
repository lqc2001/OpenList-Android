package com.openlist.android.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openlist.android.data.api.AListApiService
import com.openlist.android.data.model.LoginRequest
import com.openlist.android.data.model.LoginResponse
import com.openlist.android.data.model.SavedCredentials
import com.openlist.android.data.network.NetworkModule
import com.openlist.android.data.network.NetworkConnectivityMonitor
import com.openlist.android.data.preferences.PreferencesRepository
import com.openlist.android.data.preferences.PreferencesRepositoryImpl
import com.openlist.android.data.preferences.DataStoreModule
import com.openlist.android.data.utils.UrlUtils
import com.openlist.android.data.error.GlobalErrorHandler
import com.openlist.android.data.error.ErrorHandlingUtils
import com.openlist.android.data.error.AuthenticationException
import com.openlist.android.data.error.NetworkException
import com.openlist.android.data.error.ValidationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras

class SimpleAuthViewModel(
    val application: Application
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _savedCredentials = MutableStateFlow<SavedCredentials?>(null)
    val savedCredentials: StateFlow<SavedCredentials?> = _savedCredentials.asStateFlow()

    // 全局错误处理器
    private val globalErrorHandler = GlobalErrorHandler.getInstance(application)

    // 网络连接监控器
    private val networkMonitor = NetworkConnectivityMonitor.getInstance(application)

    // 数据存储和网络组件
    private val preferencesRepository: PreferencesRepository by lazy {
        DataStoreModule.providePreferencesRepository(
            DataStoreModule.provideDataStore(application),
            application
        )
    }

    private val apiService: AListApiService by lazy {
        NetworkModule.provideAListApiService(
            NetworkModule.provideRetrofit(
                NetworkModule.provideOkHttpClient(
                    NetworkModule.provideHttpLoggingInterceptor(),
                    application,
                    preferencesRepository
                ),
                NetworkModule.provideGson(),
                preferencesRepository
            )
        )
    }

    init {
        loadSavedCredentials()
    }

    private fun loadSavedCredentials() {
        viewModelScope.launch {
            try {
                val serverUrl = preferencesRepository.getServerUrl().first()
                val username = preferencesRepository.getUsername().first()
                val authToken = preferencesRepository.getAuthToken().first()
                val rememberMe = preferencesRepository.getRememberMe().first()

                if (authToken != null && serverUrl != null) {
                    _savedCredentials.value = SavedCredentials(
                        serverUrl = serverUrl,
                        username = username ?: "",
                        password = if (rememberMe) preferencesRepository.getPassword().first() ?: "" else "",
                        authToken = authToken,
                        rememberMe = rememberMe,
                        refreshToken = ""
                    )

                    // 验证令牌是否有效
                    validateAuthToken(authToken)
                } else {
                    _authState.value = AuthState.NotAuthenticated
                }
            } catch (e: Exception) {
                _authState.value = AuthState.NotAuthenticated
            }
        }
    }

    private suspend fun validateAuthToken(token: String) {
        try {
            val response = apiService.getCurrentUser("Bearer $token")
            if (response.isSuccessful) {
                _authState.value = AuthState.Authenticated
            } else {
                // 令牌无效，清除数据
                preferencesRepository.clearAuthData()
                _savedCredentials.value = null
                _authState.value = AuthState.NotAuthenticated
            }
        } catch (e: Exception) {
            // 网络错误或其他异常，保持离线状态
            _authState.value = AuthState.Authenticated
        }
    }

    fun login(username: String, password: String, rememberMe: Boolean) {
        viewModelScope.launch(globalErrorHandler.getCoroutineExceptionHandler()) {
            _authState.value = AuthState.Loading

            // 验证输入参数
            val validationResult = ErrorHandlingUtils.validateRequiredFields(
                mapOf(
                    "username" to username,
                    "password" to password
                )
            )

            if (validationResult is ValidationResult.Error) {
                _authState.value = AuthState.Error(validationResult.exception.message ?: "输入参数错误")
                return@launch
            }

            // 检查网络连接
            if (!networkMonitor.isConnected.value) {
                val networkInfo = networkMonitor.getCurrentNetworkInfo()
                val errorMessage = when {
                    !networkInfo.hasInternet -> "设备未连接到网络"
                    !networkInfo.isValidated -> "网络连接已建立，但无法访问互联网"
                    else -> "网络连接失败，请检查网络设置"
                }
                _authState.value = AuthState.Error(errorMessage)
                return@launch
            }

            // 检查网络质量
            if (!networkMonitor.isNetworkAvailableForApi()) {
                val recommendation = networkMonitor.getNetworkRecommendation()
                _authState.value = AuthState.Error(recommendation)
                return@launch
            }

            val savedServerUrl = preferencesRepository.getServerUrl().first()
            val serverUrl = if (savedServerUrl.isNullOrEmpty()) {
                "http://localhost:5244"
            } else {
                UrlUtils.formatUrl(savedServerUrl)
            }

            // 使用安全API调用
            ErrorHandlingUtils.safeApiCall(application) {
                val loginRequest = LoginRequest(username, password)
                apiService.login(loginRequest)
            }.collect { result ->
                result.fold(
                    onSuccess = { response ->
                        if (response.isSuccessful) {
                            val loginResponse = response.body()
                            if (loginResponse != null) {
                                // 保存认证信息
                                preferencesRepository.saveAuthToken(loginResponse.token)
                                preferencesRepository.saveUsername(username)
                                preferencesRepository.saveRememberMe(rememberMe)

                                if (rememberMe) {
                                    preferencesRepository.savePassword(password)
                                } else {
                                    preferencesRepository.savePassword("")
                                }

                                _savedCredentials.value = SavedCredentials(
                                    serverUrl = serverUrl,
                                    username = username,
                                    password = if (rememberMe) password else "",
                                    authToken = loginResponse.token,
                                    rememberMe = rememberMe,
                                    refreshToken = ""
                                )

                                _authState.value = AuthState.Authenticated
                            } else {
                                _authState.value = AuthState.Error("登录响应为空")
                            }
                        } else {
                            val errorMessage = globalErrorHandler.getApiErrorMessage(response.code())
                            _authState.value = AuthState.Error(errorMessage)
                        }
                    },
                    onFailure = { exception ->
                        val errorMessage = when (exception) {
                            is AuthenticationException -> "用户名或密码错误"
                            is NetworkException -> globalErrorHandler.getNetworkErrorMessage(exception)
                            else -> "登录失败: ${exception.message}"
                        }
                        _authState.value = AuthState.Error(errorMessage)
                    }
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val authToken = _savedCredentials.value?.authToken
                if (authToken != null) {
                    // 调用登出API
                    try {
                        apiService.logout("Bearer $authToken")
                    } catch (e: Exception) {
                        // 登出API调用失败，继续清理本地数据
                    }
                }

                // 清理本地数据
                preferencesRepository.clearAuthData()
                _savedCredentials.value = null
                _authState.value = AuthState.NotAuthenticated
            } catch (e: Exception) {
                _authState.value = AuthState.Error("退出登录失败: ${e.message}")
            }
        }
    }

    fun saveServerUrl(url: String): Boolean {
        return if (url.isEmpty()) {
            // 空URL，清除保存的URL
            viewModelScope.launch {
                preferencesRepository.saveServerUrl("")
            }
            true
        } else if (UrlUtils.isValidUrl(url)) {
            // 格式化并保存URL
            val formattedUrl = UrlUtils.formatUrl(url)
            viewModelScope.launch {
                preferencesRepository.saveServerUrl(formattedUrl)
            }
            true
        } else {
            // URL格式无效
            false
        }
    }

    fun clearError() {
        _authState.value = AuthState.NotAuthenticated
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as android.app.Application
                return SimpleAuthViewModel(application) as T
            }
        }
    }
}

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    object Authenticated : AuthState()
    object NotAuthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}