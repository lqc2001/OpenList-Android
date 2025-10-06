package com.openlist.android.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openlist.android.data.repository.AuthRepository
import com.openlist.android.data.model.SavedCredentials
import com.openlist.android.data.network.NetworkConnectivityMonitor
import com.openlist.android.data.preferences.PreferencesRepository
import com.openlist.android.data.utils.UrlUtils
import com.openlist.android.data.error.GlobalErrorHandler
import com.openlist.android.data.error.ErrorHandlingUtils
import com.openlist.android.data.error.AuthenticationException
import com.openlist.android.data.error.NetworkException
import com.openlist.android.data.error.ValidationResult
import com.openlist.android.data.error.ErrorManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SimpleAuthViewModel @Inject constructor(
    val application: Application,
    private val authRepository: AuthRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val TAG = "SimpleAuthViewModel"

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _savedCredentials = MutableStateFlow<SavedCredentials?>(null)
    val savedCredentials: StateFlow<SavedCredentials?> = _savedCredentials.asStateFlow()

    private val _autoLoginState = MutableStateFlow<AutoLoginState>(AutoLoginState.Idle)
    val autoLoginState: StateFlow<AutoLoginState> = _autoLoginState.asStateFlow()

    // 全局错误处理器
    private val globalErrorHandler = GlobalErrorHandler.getInstance(application)

    // 网络连接监控器
    private val networkMonitor = NetworkConnectivityMonitor.getInstance(application)

    // 错误管理器
    private val errorManager = ErrorManager.getInstance(application)

    // Snackbar主机状态
    private var snackbarHostState: androidx.compose.material3.SnackbarHostState? = null

    init {
        loadSavedCredentials()
    }

    private fun loadSavedCredentials() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "=== Loading saved credentials ===")
                val serverUrl = preferencesRepository.getServerUrl().first()
                val username = preferencesRepository.getUsername().first()
                val password = preferencesRepository.getPassword().first()
                val rememberMe = preferencesRepository.getRememberMe().first()
                val authToken = preferencesRepository.getAuthToken().first()

                Log.d(TAG, "Raw loaded credentials:")
                Log.d(TAG, "  - serverUrl: $serverUrl")
                Log.d(TAG, "  - username: $username")
                Log.d(TAG, "  - password: ${if (password.isNullOrEmpty()) "[empty]" else "[***hidden***]"}")
                Log.d(TAG, "  - rememberMe: $rememberMe")
                Log.d(TAG, "  - authToken: ${if (authToken.isNullOrEmpty()) "[null]" else "[***token***]"}")

                if (authToken != null && serverUrl != null) {
                    Log.d(TAG, "Found valid credentials, creating SavedCredentials object")
                    _savedCredentials.value = SavedCredentials(
                        serverUrl = serverUrl,
                        username = username ?: "",
                        password = if (rememberMe) password ?: "" else "",
                        authToken = authToken,
                        rememberMe = rememberMe,
                        refreshToken = ""
                    )
                    Log.d(TAG, "SavedCredentials object created successfully")

                    // 验证令牌是否有效
                    Log.d(TAG, "Validating auth token...")
                    validateAuthToken(authToken)
                } else {
                    Log.d(TAG, "No complete saved credentials found")
                    Log.d(TAG, "Missing: ${listOfNotNull(
                        if (authToken == null) "authToken" else null,
                        if (serverUrl == null) "serverUrl" else null
                    ).joinToString(", ")}")
                    _authState.value = AuthState.NotAuthenticated
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading saved credentials", e)
                Log.e(TAG, "Exception type: ${e.javaClass.simpleName}")
                Log.e(TAG, "Exception message: ${e.message}")
                _authState.value = AuthState.NotAuthenticated
            }
        }
    }

    fun attemptAutoLogin() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "=== Attempting auto login ===")
                _autoLoginState.value = AutoLoginState.Attempting
                Log.d(TAG, "AutoLoginState changed to: Attempting")

                val hasCredentials = preferencesRepository.hasSavedCredentials().first()
                Log.d(TAG, "Has saved credentials check result: $hasCredentials")

                if (!hasCredentials) {
                    Log.d(TAG, "No saved credentials for auto login")
                    _autoLoginState.value = AutoLoginState.NoCredentials
                    _authState.value = AuthState.NotAuthenticated
                    Log.d(TAG, "AutoLoginState changed to: NoCredentials")
                    return@launch
                }

                val serverUrl = preferencesRepository.getServerUrl().first()
                val username = preferencesRepository.getUsername().first()
                val password = preferencesRepository.getPassword().first()
                val rememberMe = preferencesRepository.getRememberMe().first()

                Log.d(TAG, "Retrieved credentials for auto login:")
                Log.d(TAG, "  - serverUrl: $serverUrl")
                Log.d(TAG, "  - username: $username")
                Log.d(TAG, "  - password: ${if (password.isNullOrEmpty()) "[empty]" else "[***hidden***]"}")
                Log.d(TAG, "  - rememberMe: $rememberMe")

                if (serverUrl.isNullOrEmpty() || username.isNullOrEmpty() || password.isNullOrEmpty()) {
                    Log.d(TAG, "Invalid saved credentials detected")
                    Log.d(TAG, "Missing fields: ${listOfNotNull(
                        if (serverUrl.isNullOrEmpty()) "serverUrl" else null,
                        if (username.isNullOrEmpty()) "username" else null,
                        if (password.isNullOrEmpty()) "password" else null
                    ).joinToString(", ")}")
                    Log.d(TAG, "Clearing invalid credentials")
                    preferencesRepository.clearCredentials()
                    _autoLoginState.value = AutoLoginState.InvalidCredentials
                    _authState.value = AuthState.NotAuthenticated
                    Log.d(TAG, "AutoLoginState changed to: InvalidCredentials")
                    return@launch
                }

                Log.d(TAG, "All required credentials present, performing auto login")
                Log.d(TAG, "Login parameters: username=$username, rememberMe=$rememberMe, serverUrl=$serverUrl")
                performAutoLogin(username, password, rememberMe, serverUrl)

            } catch (e: Exception) {
                Log.e(TAG, "Error during auto login", e)
                Log.e(TAG, "Exception type: ${e.javaClass.simpleName}")
                Log.e(TAG, "Exception message: ${e.message}")
                _autoLoginState.value = AutoLoginState.Failed("自动登录失败: ${e.message}")
                _authState.value = AuthState.NotAuthenticated
                Log.d(TAG, "AutoLoginState changed to: Failed")
            }
        }
    }

    private suspend fun performAutoLogin(username: String, password: String, rememberMe: Boolean, serverUrl: String) {
        try {
            Log.d(TAG, "=== Performing auto login ===")
            Log.d(TAG, "Auto login parameters:")
            Log.d(TAG, "  - username: $username")
            Log.d(TAG, "  - password: [***hidden***]")
            Log.d(TAG, "  - rememberMe: $rememberMe")
            Log.d(TAG, "  - serverUrl: $serverUrl")

            // 检查网络连接
            Log.d(TAG, "Checking network connectivity...")
            if (!networkMonitor.isConnected.value) {
                Log.d(TAG, "Network not connected")
                val networkInfo = networkMonitor.getCurrentNetworkInfo()
                Log.d(TAG, "Network info: hasInternet=${networkInfo.hasInternet}, isValidated=${networkInfo.isValidated}")
                val errorMessage = when {
                    !networkInfo.hasInternet -> "设备未连接到网络"
                    !networkInfo.isValidated -> "网络连接已建立，但无法访问互联网"
                    else -> "网络连接失败，请检查网络设置"
                }
                Log.e(TAG, "Network check failed: $errorMessage")
                _autoLoginState.value = AutoLoginState.Failed(errorMessage)
                _authState.value = AuthState.NotAuthenticated
                return
            }
            Log.d(TAG, "Network connectivity check passed")

            // 检查网络质量
            Log.d(TAG, "Checking network quality...")
            if (!networkMonitor.isNetworkAvailableForApi()) {
                Log.d(TAG, "Network quality not sufficient for API calls")
                val recommendation = networkMonitor.getNetworkRecommendation()
                Log.e(TAG, "Network quality check failed: $recommendation")
                _autoLoginState.value = AutoLoginState.Failed(recommendation)
                _authState.value = AuthState.NotAuthenticated
                return
            }
            Log.d(TAG, "Network quality check passed")

            Log.d(TAG, "Starting auth repository login...")
            authRepository.login(username, password).collect { result ->
                result.fold(
                    onSuccess = { loginResponse ->
                        try {
                            Log.d(TAG, "Auto login successful")

                            // 保存认证信息
                            preferencesRepository.saveRememberMe(rememberMe)
                            preferencesRepository.saveUsername(username)
                            preferencesRepository.saveAuthToken(loginResponse.data?.token ?: "")

                            if (rememberMe) {
                                preferencesRepository.savePassword(password)
                            } else {
                                preferencesRepository.savePassword("")
                            }

                            _savedCredentials.value = SavedCredentials(
                                serverUrl = serverUrl,
                                username = username,
                                password = if (rememberMe) password else "",
                                authToken = loginResponse.data?.token ?: "",
                                rememberMe = rememberMe,
                                refreshToken = ""
                            )

                            _autoLoginState.value = AutoLoginState.Success
                            _authState.value = AuthState.Authenticated
                        } catch (saveException: Exception) {
                            Log.e(TAG, "Error saving auto login credentials", saveException)
                            // 即使保存失败，也认为登录成功
                            _savedCredentials.value = SavedCredentials(
                                serverUrl = serverUrl,
                                username = username,
                                password = "",
                                authToken = loginResponse.data?.token ?: "",
                                rememberMe = false,
                                refreshToken = ""
                            )
                            _autoLoginState.value = AutoLoginState.Success
                            _authState.value = AuthState.Authenticated
                        }
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Auto login failed", exception)
                        val errorMessage = when (exception) {
                            is AuthenticationException -> "用户名或密码错误"
                            is NetworkException -> "网络错误: ${exception.message}"
                            else -> "登录失败: ${exception.message}"
                        }

                        // 使用错误管理器处理错误
                        viewModelScope.launch {
                            errorManager.addException(exception) {
                                // 重试自动登录
                                viewModelScope.launch {
                                    performAutoLogin(username, password, rememberMe, serverUrl)
                                }
                            }
                        }

                        _autoLoginState.value = AutoLoginState.Failed(errorMessage)
                        _authState.value = AuthState.Error(errorMessage)
                    }
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during auto login", e)
            _autoLoginState.value = AutoLoginState.Failed("自动登录过程发生异常: ${e.message}")
            _authState.value = AuthState.Error("自动登录过程发生异常: ${e.message}")
        }
    }

    fun cancelAutoLogin() {
        Log.d(TAG, "Auto login cancelled")
        _autoLoginState.value = AutoLoginState.Cancelled
        _authState.value = AuthState.NotAuthenticated
    }

    private suspend fun validateAuthToken(token: String) {
        authRepository.getCurrentUser().collect { result ->
            result.fold(
                onSuccess = { user ->
                    Log.d(TAG, "Auth token validated successfully")
                    _authState.value = AuthState.Authenticated
                },
                onFailure = { exception ->
                    Log.e(TAG, "Auth token validation failed", exception)
                    // 令牌无效，清除数据
                    preferencesRepository.clearCredentials()
                    _savedCredentials.value = null
                    _authState.value = AuthState.NotAuthenticated
                }
            )
        }
    }

    fun login(username: String, password: String, rememberMe: Boolean) {
        viewModelScope.launch(globalErrorHandler.getCoroutineExceptionHandler()) {
            Log.d(TAG, "=== Manual login attempt ===")
            Log.d(TAG, "Login parameters:")
            Log.d(TAG, "  - username: $username")
            Log.d(TAG, "  - password: [***hidden***]")
            Log.d(TAG, "  - rememberMe: $rememberMe")
            _authState.value = AuthState.Loading
            Log.d(TAG, "AuthState changed to: Loading")

            // 验证输入参数
            Log.d(TAG, "Validating input parameters...")
            val validationResult = ErrorHandlingUtils.validateRequiredFields(
                mapOf(
                    "username" to username,
                    "password" to password
                )
            )

            if (validationResult is ValidationResult.Error) {
                Log.e(TAG, "Validation failed: ${validationResult.exception.message}")
                Log.e(TAG, "Validation exception type: ${validationResult.exception.javaClass.simpleName}")
                _authState.value = AuthState.Error(validationResult.exception.message ?: "输入参数错误")
                Log.d(TAG, "AuthState changed to: Error")
                return@launch
            }
            Log.d(TAG, "Input parameter validation passed")

            // 检查网络连接
            Log.d(TAG, "Checking network connectivity...")
            if (!networkMonitor.isConnected.value) {
                Log.d(TAG, "Network not connected")
                val networkInfo = networkMonitor.getCurrentNetworkInfo()
                Log.d(TAG, "Network info: hasInternet=${networkInfo.hasInternet}, isValidated=${networkInfo.isValidated}")
                val errorMessage = when {
                    !networkInfo.hasInternet -> "设备未连接到网络"
                    !networkInfo.isValidated -> "网络连接已建立，但无法访问互联网"
                    else -> "网络连接失败，请检查网络设置"
                }
                Log.e(TAG, "Network check failed: $errorMessage")
                _authState.value = AuthState.Error(errorMessage)
                Log.d(TAG, "AuthState changed to: Error")
                return@launch
            }
            Log.d(TAG, "Network connectivity check passed")

            // 检查网络质量
            Log.d(TAG, "Checking network quality...")
            if (!networkMonitor.isNetworkAvailableForApi()) {
                Log.d(TAG, "Network quality not sufficient for API calls")
                val recommendation = networkMonitor.getNetworkRecommendation()
                Log.e(TAG, "Network quality check failed: $recommendation")
                _authState.value = AuthState.Error(recommendation)
                Log.d(TAG, "AuthState changed to: Error")
                return@launch
            }
            Log.d(TAG, "Network quality check passed")

            val savedServerUrl = preferencesRepository.getServerUrl().first()
            Log.d(TAG, "Retrieved saved server URL: $savedServerUrl")

            val serverUrl = if (savedServerUrl.isNullOrEmpty()) {
                Log.d(TAG, "No saved server URL, using default")
                "http://localhost:5244"
            } else {
                Log.d(TAG, "Processing saved server URL...")
                val url = savedServerUrl.trim()
                val processedUrl = when {
                    url.startsWith("http://") -> {
                        Log.d(TAG, "URL already has http protocol")
                        url
                    }
                    url.startsWith("https://") -> {
                        Log.d(TAG, "Converting https to http protocol")
                        url.replace("https://", "http://")
                    }
                    else -> {
                        Log.d(TAG, "Adding http protocol to URL")
                        "http://$url"
                    }
                }

                // 修复DNS解析问题 - 如果是特定域名，直接使用正确的IP
                val finalUrl = when {
                    processedUrl.contains("j.yzfycz.cn") -> {
                        Log.d(TAG, "Applying DNS fix for j.yzfycz.cn domain")
                        processedUrl.replace("j.yzfycz.cn", "140.250.217.58")
                    }
                    processedUrl.contains("yzfycz.cn") -> {
                        Log.d(TAG, "Applying DNS fix for yzfycz.cn domain")
                        processedUrl.replace("yzfycz.cn", "140.250.217.58")
                    }
                    else -> processedUrl
                }
                Log.d(TAG, "Final processed URL: $finalUrl")
                finalUrl
            }

            Log.d(TAG, "Using server URL: $serverUrl")

            // 使用AuthRepository进行登录
            try {
                authRepository.login(username, password).collect { result ->
                    result.fold(
                        onSuccess = { loginResponse ->
                            try {
                                Log.d(TAG, "Manual login successful")

                                // 保存认证信息
                                preferencesRepository.saveRememberMe(rememberMe)

                                preferencesRepository.saveUsername(username)
                                preferencesRepository.saveAuthToken(loginResponse.data?.token ?: "")

                                if (rememberMe) {
                                    preferencesRepository.savePassword(password)
                                } else {
                                    preferencesRepository.savePassword("")
                                }

                                _savedCredentials.value = SavedCredentials(
                                    serverUrl = serverUrl,
                                    username = username,
                                    password = if (rememberMe) password else "",
                                    authToken = loginResponse.data?.token ?: "",
                                    rememberMe = rememberMe,
                                    refreshToken = ""
                                )

                                _authState.value = AuthState.Authenticated
                            } catch (saveException: Exception) {
                                Log.e(TAG, "Error saving login credentials", saveException)
                                // 即使保存失败，也认为登录成功
                                _savedCredentials.value = SavedCredentials(
                                    serverUrl = serverUrl,
                                    username = username,
                                    password = "",
                                    authToken = loginResponse.data?.token ?: "",
                                    rememberMe = false,
                                    refreshToken = ""
                                )
                                _authState.value = AuthState.Authenticated
                            }
                        },
                        onFailure = { exception ->
                            Log.e(TAG, "Manual login failed", exception)
                            val errorMessage = when (exception) {
                                is AuthenticationException -> "用户名或密码错误"
                                is NetworkException -> "网络错误: ${exception.message}"
                                else -> "登录失败: ${exception.message}"
                            }

                            // 使用错误管理器处理错误
                            viewModelScope.launch {
                                errorManager.addException(exception) {
                                    // 简单重试，避免递归
                                    Log.d(TAG, "User requested retry for login")
                                    // 这里可以添加更简单的重试逻辑
                                }
                            }

                            _authState.value = AuthState.Error(errorMessage)
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during manual login", e)
                _authState.value = AuthState.Error("登录过程发生异常: ${e.message}")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            Log.d(TAG, "=== Logout initiated ===")
            _authState.value = AuthState.Loading
            Log.d(TAG, "AuthState changed to: Loading")

            try {
                Log.d(TAG, "Starting auth repository logout...")
                // 使用AuthRepository进行登出
                authRepository.logout().collect { result ->
                    result.fold(
                        onSuccess = {
                            Log.d(TAG, "Logout API call successful")
                            // 登出成功，清理本地数据
                            Log.d(TAG, "Clearing local credentials and data...")
                            preferencesRepository.clearCredentials()
                            _savedCredentials.value = null
                            _authState.value = AuthState.NotAuthenticated
                            Log.d(TAG, "AuthState changed to: NotAuthenticated")
                            Log.d(TAG, "Logout process completed successfully")
                        },
                        onFailure = { exception ->
                            Log.e(TAG, "Logout API call failed", exception)
                            Log.e(TAG, "Logout exception type: ${exception.javaClass.simpleName}")
                            Log.e(TAG, "Logout exception message: ${exception.message}")
                            // 即使登出失败，也清理本地数据
                            Log.d(TAG, "API logout failed, clearing local data anyway...")
                            preferencesRepository.clearCredentials()
                            _savedCredentials.value = null
                            _authState.value = AuthState.NotAuthenticated
                            Log.d(TAG, "AuthState changed to: NotAuthenticated")
                            Log.d(TAG, "Local data cleared despite API failure")
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during logout", e)
                Log.e(TAG, "Logout exception type: ${e.javaClass.simpleName}")
                Log.e(TAG, "Logout exception message: ${e.message}")
                // 异常情况下也清理本地数据
                Log.d(TAG, "Exception occurred, clearing local data...")
                preferencesRepository.clearCredentials()
                _savedCredentials.value = null
                _authState.value = AuthState.NotAuthenticated
                Log.d(TAG, "AuthState changed to: NotAuthenticated")
                Log.d(TAG, "Local data cleared despite exception")
            }
        }
    }

    fun saveServerUrl(url: String): Boolean {
        Log.d(TAG, "=== Save server URL request ===")
        Log.d(TAG, "Input URL: '$url'")

        return if (url.isEmpty()) {
            Log.d(TAG, "Empty URL provided, clearing saved server URL")
            // 空URL，清除保存的URL
            viewModelScope.launch {
                try {
                    Log.d(TAG, "Clearing saved server URL in preferences...")
                    preferencesRepository.saveServerUrl("")
                    Log.d(TAG, "Server URL cleared successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Error clearing server URL", e)
                    Log.e(TAG, "Exception type: ${e.javaClass.simpleName}")
                    Log.e(TAG, "Exception message: ${e.message}")
                }
            }
            true
        } else {
            Log.d(TAG, "Non-empty URL provided, processing...")
            // 强制使用HTTP并保存URL，同时修复DNS问题
            val formattedUrl = url.trim().let { u ->
                Log.d(TAG, "Processing URL: '$u'")
                val processedUrl = when {
                    u.startsWith("http://") -> {
                        Log.d(TAG, "URL already has http protocol")
                        u
                    }
                    u.startsWith("https://") -> {
                        Log.d(TAG, "Converting https to http protocol")
                        u.replace("https://", "http://")
                    }
                    else -> {
                        Log.d(TAG, "Adding http protocol to URL")
                        "http://$u"
                    }
                }

                // 修复DNS解析问题 - 如果是特定域名，直接使用正确的IP
                val finalUrl = when {
                    processedUrl.contains("j.yzfycz.cn") -> {
                        Log.d(TAG, "Applying DNS fix for j.yzfycz.cn domain")
                        processedUrl.replace("j.yzfycz.cn", "140.250.217.58")
                    }
                    processedUrl.contains("yzfycz.cn") -> {
                        Log.d(TAG, "Applying DNS fix for yzfycz.cn domain")
                        processedUrl.replace("yzfycz.cn", "140.250.217.58")
                    }
                    else -> {
                        Log.d(TAG, "No DNS fix needed for this URL")
                        processedUrl
                    }
                }
                Log.d(TAG, "Final formatted URL: $finalUrl")
                finalUrl
            }
            viewModelScope.launch {
                try {
                    Log.d(TAG, "Saving formatted server URL to preferences...")
                    preferencesRepository.saveServerUrl(formattedUrl)
                    Log.d(TAG, "Server URL saved successfully: $formattedUrl")
                } catch (e: Exception) {
                    Log.e(TAG, "Error saving server URL", e)
                    Log.e(TAG, "Exception type: ${e.javaClass.simpleName}")
                    Log.e(TAG, "Exception message: ${e.message}")
                }
            }
            true
        }
    }

    fun clearError() {
        Log.d(TAG, "Clearing error state")
        Log.d(TAG, "Previous auth state: ${_authState.value}")
        _authState.value = AuthState.NotAuthenticated
        Log.d(TAG, "AuthState changed to: NotAuthenticated")
    }

    fun setErrorManagerSnackbarHost(snackbarHostState: androidx.compose.material3.SnackbarHostState) {
        Log.d(TAG, "Setting SnackbarHostState for error manager")
        this.snackbarHostState = snackbarHostState
        errorManager.setSnackbarHostState(snackbarHostState)
        Log.d(TAG, "SnackbarHostState set successfully")
    }

    fun clearCredentials() {
        Log.d(TAG, "=== Clearing credentials ===")
        viewModelScope.launch {
            try {
                Log.d(TAG, "Clearing credentials through preferences repository...")
                preferencesRepository.clearCredentials()
                _savedCredentials.value = null
                Log.d(TAG, "Credentials cleared successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing credentials", e)
                Log.e(TAG, "Exception type: ${e.javaClass.simpleName}")
                Log.e(TAG, "Exception message: ${e.message}")
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

sealed class AutoLoginState {
    object Idle : AutoLoginState()
    object Attempting : AutoLoginState()
    object Success : AutoLoginState()
    object NoCredentials : AutoLoginState()
    object InvalidCredentials : AutoLoginState()
    object Cancelled : AutoLoginState()
    data class Failed(val message: String) : AutoLoginState()
}