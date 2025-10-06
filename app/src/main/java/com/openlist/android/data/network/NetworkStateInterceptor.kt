package com.openlist.android.data.network

import android.content.Context
import com.openlist.android.data.utils.UrlUtils
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class NetworkStateInterceptor(
    private val context: Context,
    private val networkMonitor: NetworkConnectivityMonitor
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        // 检查网络连接状态
        if (!networkMonitor.isConnected.value) {
            throw IOException("网络未连接，请检查网络设置")
        }

        // 检查网络质量
        if (!networkMonitor.isNetworkAvailableForApi()) {
            val recommendation = networkMonitor.getNetworkRecommendation()
            throw IOException(recommendation)
        }

        val request = chain.request()
        val url = request.url.toString()

        try {
            val response = chain.proceed(request)
            return response
        } catch (e: Exception) {
            throw when (e) {
                is SocketTimeoutException -> {
                    // 根据URL类型提供更具体的超时错误信息
                    val host = UrlUtils.getHost(url)
                    val timeoutMessage = when {
                        host.contains("localhost") || host.contains("127.0.0.1") ->
                            "连接本地服务器超时: ${e.message}，请确认本地AList服务正在运行"
                        host.contains("192.168.") ->
                            "连接内网服务器超时: ${e.message}，请确认服务器在同一个网络中"
                        else ->
                            "连接服务器超时: ${e.message}"
                    }
                    IOException(timeoutMessage)
                }
                is ConnectException -> {
                    val host = UrlUtils.getHost(url)
                    val connectMessage = when {
                        host.contains("localhost") || host.contains("127.0.0.1") ->
                            "无法连接到本地服务器: ${e.message}，请确认AList服务正在运行"
                        host.contains("192.168.") ->
                            "无法连接到内网服务器: ${e.message}，请确认服务器地址和网络配置"
                        else ->
                            "无法连接到服务器: ${e.message}"
                    }
                    IOException(connectMessage)
                }
                is UnknownHostException -> {
                    val host = UrlUtils.getHost(url)
                    IOException("无法解析服务器地址: $host (${e.message})")
                }
                else -> {
                    IOException("网络请求失败: ${e.javaClass.simpleName}: ${e.message}")
                }
            }
        }
    }
}