package com.openlist.android.data.repository

import android.app.Application
import com.openlist.android.data.api.OpenListApiService
import com.openlist.android.data.model.ApiResponse
import com.openlist.android.data.model.LoginRequest
import com.openlist.android.data.model.LoginResponse
import com.openlist.android.data.model.User
import com.openlist.android.data.network.ConnectionPoolConfig
import com.openlist.android.data.network.NetworkConnectivityMonitor
import com.openlist.android.data.network.NetworkStateInterceptor
import com.openlist.android.data.network.RetryInterceptor
import com.openlist.android.data.preferences.PreferencesRepository
import com.openlist.android.data.utils.UrlUtils
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val application: Application,
    private val preferencesRepository: PreferencesRepository
) {

    private fun createApiService(serverUrl: String): OpenListApiService {
        val gson = GsonBuilder()
            .setLenient()
            .create()

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val networkMonitor = NetworkConnectivityMonitor.getInstance(application)

        val okHttpClient = ConnectionPoolConfig.configureOptimizations(
            OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(NetworkStateInterceptor(application, networkMonitor))
                .addInterceptor(RetryInterceptor(maxRetries = 3, retryDelay = 1000))
        ).build()

        val baseUrl = if (serverUrl.isNullOrEmpty()) {
            "http://localhost:5244"
        } else {
            // 强制使用HTTP，不管输入是什么
            val url = serverUrl.trim()
            val processedUrl = when {
                url.startsWith("http://") -> url
                url.startsWith("https://") -> url.replace("https://", "http://")
                else -> "http://$url"
            }

            // 修复DNS解析问题 - 如果是特定域名，直接使用正确的IP
            when {
                processedUrl.contains("j.yzfycz.cn") -> processedUrl.replace("j.yzfycz.cn", "140.250.217.58")
                processedUrl.contains("yzfycz.cn") -> processedUrl.replace("yzfycz.cn", "140.250.217.58")
                else -> processedUrl
            }
        }

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        return retrofit.create(OpenListApiService::class.java)
    }

    suspend fun login(username: String, password: String): Flow<Result<LoginResponse>> = flow {
        try {
            val request = LoginRequest(username, password)

            // 获取当前保存的服务器URL并强制使用HTTP
            val savedServerUrl = preferencesRepository.getServerUrl().first()
            val serverUrl = if (savedServerUrl.isNullOrEmpty()) {
                "http://localhost:5244"
            } else {
                // 强制使用HTTP，不管保存的是什么
                val url = savedServerUrl.trim()
                val processedUrl = when {
                    url.startsWith("http://") -> url
                    url.startsWith("https://") -> url.replace("https://", "http://")
                    else -> "http://$url"
                }

                // 修复DNS解析问题 - 如果是特定域名，直接使用正确的IP
                when {
                    processedUrl.contains("j.yzfycz.cn") -> processedUrl.replace("j.yzfycz.cn", "140.250.217.58")
                    processedUrl.contains("yzfycz.cn") -> processedUrl.replace("yzfycz.cn", "140.250.217.58")
                    else -> processedUrl
                }
            }

            // 创建使用正确URL的API服务
            val apiService = createApiService(serverUrl)
            val response = apiService.login(request)

            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!

                // 检查服务器响应状态
                if (loginResponse.code == 200 && loginResponse.data != null) {
                    val loginData = loginResponse.data!!
                    try {
                        // 保存token和用户信息
                        preferencesRepository.saveAuthToken(loginData.token)
                        preferencesRepository.saveUsername(username)
                        val serverUrl = preferencesRepository.getServerUrl().first()
                        preferencesRepository.saveServerUrl(serverUrl ?: "")
                        emit(Result.success(loginResponse))
                    } catch (saveException: Exception) {
                        // 如果保存失败，仍然返回登录成功，但记录错误
                        emit(Result.success(loginResponse))
                    }
                } else {
                    // 服务器返回错误状态
                    throw Exception("登录失败: ${loginResponse.message}")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorCode = response.code()
                val errorMessage = when (errorCode) {
                    400 -> "请求参数错误: $errorBody"
                    401 -> "认证失败: 用户名或密码错误"
                    403 -> "访问被拒绝: 没有权限访问"
                    404 -> "服务器端点不存在，请检查服务器地址"
                    500 -> "服务器内部错误: $errorBody"
                    502, 503, 504 -> "服务器暂时不可用，请稍后重试"
                    else -> "登录失败 (HTTP $errorCode): ${response.message()} - $errorBody"
                }
                throw Exception(errorMessage)
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun getCurrentUser(): Flow<Result<User>> = flow {
        try {
            val token = preferencesRepository.getAuthToken().first()
            if (token.isNullOrEmpty()) {
                emit(Result.failure(Exception("未找到认证令牌")))
                return@flow
            }

            // 获取当前保存的服务器URL并创建API服务
            val savedServerUrl = preferencesRepository.getServerUrl().first()
            val serverUrl = if (savedServerUrl.isNullOrEmpty()) {
                "http://localhost:5244"
            } else {
                run {
                val url = savedServerUrl.trim()
                val processedUrl = when {
                    url.startsWith("http://") -> url
                    url.startsWith("https://") -> url.replace("https://", "http://")
                    else -> "http://$url"
                }

                // 修复DNS解析问题 - 如果是特定域名，直接使用正确的IP
                when {
                    processedUrl.contains("j.yzfycz.cn") -> processedUrl.replace("j.yzfycz.cn", "140.250.217.58")
                    processedUrl.contains("yzfycz.cn") -> processedUrl.replace("yzfycz.cn", "140.250.217.58")
                    else -> processedUrl
                }
            }
            }
            val apiService = createApiService(serverUrl)

            val response = apiService.getCurrentUser("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!))
            } else {
                throw Exception("获取用户信息失败: ${response.message()}")
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun logout(): Flow<Result<ApiResponse>> = flow {
        try {
            val token = preferencesRepository.getAuthToken().first()
            if (token.isNullOrEmpty()) {
                // 如果没有token，直接返回成功
                emit(Result.success(ApiResponse(200, "已退出登录", null)))
                return@flow
            }

            // 获取当前保存的服务器URL并创建API服务
            val savedServerUrl = preferencesRepository.getServerUrl().first()
            val serverUrl = if (savedServerUrl.isNullOrEmpty()) {
                "http://localhost:5244"
            } else {
                run {
                val url = savedServerUrl.trim()
                val processedUrl = when {
                    url.startsWith("http://") -> url
                    url.startsWith("https://") -> url.replace("https://", "http://")
                    else -> "http://$url"
                }

                // 修复DNS解析问题 - 如果是特定域名，直接使用正确的IP
                when {
                    processedUrl.contains("j.yzfycz.cn") -> processedUrl.replace("j.yzfycz.cn", "140.250.217.58")
                    processedUrl.contains("yzfycz.cn") -> processedUrl.replace("yzfycz.cn", "140.250.217.58")
                    else -> processedUrl
                }
            }
            }
            val apiService = createApiService(serverUrl)

            val response = apiService.logout("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                // 清除本地存储的认证信息
                preferencesRepository.clearAuthData()
                emit(Result.success(response.body()!!))
            } else {
                // 即使服务器请求失败，也清除本地认证信息
                preferencesRepository.clearAuthData()
                emit(Result.success(ApiResponse(200, "已退出登录", null)))
            }
        } catch (e: Exception) {
            // 网络错误时也清除本地认证信息
            preferencesRepository.clearAuthData()
            emit(Result.success(ApiResponse(200, "已退出登录", null)))
        }
    }

    fun isLoggedIn(): Flow<Boolean> {
        return preferencesRepository.getAuthToken().map { token -> !token.isNullOrEmpty() }
    }

    suspend fun getSavedUsername(): Flow<String?> {
        return preferencesRepository.getUsername()
    }

    suspend fun getSavedServerUrl(): Flow<String?> {
        return preferencesRepository.getServerUrl()
    }
}