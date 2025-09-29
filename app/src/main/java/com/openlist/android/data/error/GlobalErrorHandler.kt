package com.openlist.android.data.error

import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

class GlobalErrorHandler(private val context: Context) {

    fun getCoroutineExceptionHandler(): CoroutineExceptionHandler {
        return CoroutineExceptionHandler { _, throwable ->
            handleError(throwable)
        }
    }

    fun handleError(throwable: Throwable) {
        val errorMessage = when (throwable) {
            is UnknownHostException -> "网络连接失败，请检查网络设置"
            is SocketTimeoutException -> "连接超时，请重试"
            is SSLException -> "安全连接失败，请检查网络环境"
            is IOException -> "网络错误，请检查连接后重试"
            is SecurityException -> "安全验证失败"
            is IllegalStateException -> "应用程序状态异常"
            is IllegalArgumentException -> "参数错误"
            else -> "发生未知错误: ${throwable.message}"
        }

        // 在主线程显示错误信息
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }

        // 记录错误日志
        logError(throwable)
    }

    fun getNetworkErrorMessage(throwable: Throwable): String {
        return when (throwable) {
            is UnknownHostException -> "无法连接到服务器，请检查服务器地址是否正确"
            is SocketTimeoutException -> "连接超时，请检查网络信号后重试"
            is SSLException -> "安全连接失败，请检查网络环境或服务器证书"
            is IOException -> "网络连接失败，请检查网络设置"
            is java.net.ConnectException -> "连接被拒绝，服务器可能未启动"
            is java.net.NoRouteToHostException -> "无法到达服务器，请检查网络配置"
            else -> "网络请求失败: ${throwable.message}"
        }
    }

    fun getApiErrorMessage(errorCode: Int): String {
        return when (errorCode) {
            400 -> "请求参数错误，请检查输入"
            401 -> "认证失败，请重新登录"
            403 -> "访问被拒绝，请联系管理员"
            404 -> "请求的资源不存在"
            413 -> "请求体过大"
            422 -> "请求格式错误"
            429 -> "请求过于频繁，请稍后重试"
            500 -> "服务器内部错误，请稍后重试"
            502 -> "网关错误，服务器维护中"
            503 -> "服务不可用，请稍后重试"
            504 -> "网关超时，请检查网络连接"
            else -> "请求失败 ($errorCode)"
        }
    }

    private fun logError(throwable: Throwable) {
        // 这里可以实现更详细的错误日志记录
        // 例如发送到崩溃分析服务或写入本地日志文件
        throwable.printStackTrace()
    }

    fun validateNetworkState(): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
            val network = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
            networkCapabilities != null &&
            (networkCapabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
             networkCapabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_VALIDATED))
        } catch (e: Exception) {
            // 降级到旧API作为备用
            try {
                val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
                val activeNetworkInfo = connectivityManager.activeNetworkInfo
                activeNetworkInfo != null && activeNetworkInfo.isConnected
            } catch (e2: Exception) {
                false
            }
        }
    }

    fun <T> createSafeResult(
        operation: () -> T,
        onError: (Throwable) -> Unit = { handleError(it) }
    ): Result<T> {
        return try {
            Result.success(operation())
        } catch (e: Exception) {
            onError(e)
            Result.failure(e)
        }
    }

    companion object {
        @Volatile
        private var instance: GlobalErrorHandler? = null

        fun getInstance(context: Context): GlobalErrorHandler {
            return instance ?: synchronized(this) {
                instance ?: GlobalErrorHandler(context.applicationContext).also { instance = it }
            }
        }
    }
}