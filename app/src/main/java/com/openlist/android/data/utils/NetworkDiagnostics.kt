package com.openlist.android.data.utils

import android.content.Context
import com.openlist.android.data.network.NetworkConnectivityMonitor
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import java.net.HttpURLConnection
import java.net.URL

object NetworkDiagnostics {

    /**
     * 测试服务器连接性
     */
    suspend fun testServerConnection(
        serverUrl: String,
        timeoutMs: Long = 10000
    ): NetworkTestResult {
        return try {
            withTimeout(timeoutMs) {
                val formattedUrl = UrlUtils.formatUrl(serverUrl)
                val url = URL("$formattedUrl/api/auth/me")
                val connection = url.openConnection() as HttpURLConnection

                // 设置连接参数
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.setRequestProperty("User-Agent", "AList-Android/1.0.0")

                // 测试连接
                connection.connect()
                val responseCode = connection.responseCode
                connection.disconnect()

                when (responseCode) {
                    200 -> NetworkTestResult.Success
                    401 -> NetworkTestResult.AuthRequired
                    404 -> NetworkTestResult.ServerNotFound
                    in 500..599 -> NetworkTestResult.ServerError
                    else -> NetworkTestResult.UnknownError(responseCode)
                }
            }
        } catch (e: Exception) {
            when (e) {
                is java.net.SocketTimeoutException -> NetworkTestResult.Timeout
                is java.net.ConnectException -> NetworkTestResult.ConnectionFailed
                is java.net.UnknownHostException -> NetworkTestResult.HostNotFound
                else -> NetworkTestResult.NetworkError(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * 获取详细的网络诊断信息
     */
    suspend fun getNetworkDiagnostics(
        context: Context,
        serverUrl: String
    ): NetworkDiagnosticsInfo {
        val networkMonitor = NetworkConnectivityMonitor.getInstance(context)
        val networkInfo = networkMonitor.getCurrentNetworkInfo()

        return NetworkDiagnosticsInfo(
            isConnected = networkInfo.isConnected,
            connectionType = networkInfo.connectionType,
            connectionQuality = networkInfo.connectionQuality,
            hasInternet = networkInfo.hasInternet,
            isValidated = networkInfo.isValidated,
            isMetered = networkInfo.isMetered,
            serverUrl = serverUrl,
            formattedUrl = UrlUtils.formatUrl(serverUrl) ?: "",
            testResult = testServerConnection(serverUrl)
        )
    }

    /**
     * 优化建议
     */
    fun getOptimizationSuggestions(diagnostics: NetworkDiagnosticsInfo): List<String> {
        val suggestions = mutableListOf<String>()

        when {
            !diagnostics.isConnected -> {
                suggestions.add("请检查网络连接")
            }
            !diagnostics.hasInternet -> {
                suggestions.add("网络连接已建立，但无法访问互联网")
            }
            !diagnostics.isValidated -> {
                suggestions.add("网络需要验证，请检查 captive portal")
            }
            diagnostics.connectionQuality == NetworkConnectivityMonitor.ConnectionQuality.POOR -> {
                suggestions.add("网络质量较差，建议切换到WiFi网络")
            }
            diagnostics.isMetered -> {
                suggestions.add("当前使用移动网络，可能产生流量费用")
            }
        }

        when (diagnostics.testResult) {
            is NetworkTestResult.Timeout -> {
                suggestions.add("服务器响应超时，请检查网络或稍后重试")
            }
            is NetworkTestResult.ConnectionFailed -> {
                suggestions.add("无法连接到服务器，请确认服务器地址正确")
            }
            is NetworkTestResult.HostNotFound -> {
                suggestions.add("无法解析服务器地址，请检查URL格式")
            }
            is NetworkTestResult.AuthRequired -> {
                suggestions.add("服务器正常运行，需要登录认证")
            }
            is NetworkTestResult.ServerNotFound -> {
                suggestions.add("服务器未找到，请确认AList服务已启动")
            }
            is NetworkTestResult.ServerError -> {
                suggestions.add("服务器内部错误，请联系服务器管理员")
            }
            else -> {}
        }

        return suggestions
    }
}

/**
 * 网络测试结果
 */
sealed class NetworkTestResult {
    object Success : NetworkTestResult()
    object AuthRequired : NetworkTestResult()
    object ServerNotFound : NetworkTestResult()
    object ServerError : NetworkTestResult()
    object Timeout : NetworkTestResult()
    object ConnectionFailed : NetworkTestResult()
    object HostNotFound : NetworkTestResult()
    data class UnknownError(val code: Int) : NetworkTestResult()
    data class NetworkError(val message: String) : NetworkTestResult()
}

/**
 * 网络诊断信息
 */
data class NetworkDiagnosticsInfo(
    val isConnected: Boolean,
    val connectionType: NetworkConnectivityMonitor.ConnectionType,
    val connectionQuality: NetworkConnectivityMonitor.ConnectionQuality,
    val hasInternet: Boolean,
    val isValidated: Boolean,
    val isMetered: Boolean,
    val serverUrl: String,
    val formattedUrl: String,
    val testResult: NetworkTestResult
)