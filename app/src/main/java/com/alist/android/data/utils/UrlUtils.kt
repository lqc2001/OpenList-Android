package com.alist.android.data.utils

import java.util.regex.Pattern

object UrlUtils {

    private val URL_PATTERN = Pattern.compile(
        "^https?://.+",
        Pattern.CASE_INSENSITIVE
    )

    /**
     * 格式化URL，确保包含协议前缀
     */
    fun formatUrl(inputUrl: String): String {
        var url = inputUrl.trim()

        // 如果为空，返回默认URL
        if (url.isEmpty()) {
            return "http://localhost:5244"
        }

        // 如果已经包含协议，直接返回
        if (URL_PATTERN.matcher(url).matches()) {
            return url
        }

        // 添加默认的https协议
        return "https://$url"
    }

    /**
     * 验证URL格式
     */
    fun isValidUrl(url: String): Boolean {
        if (url.isEmpty()) return false

        val formattedUrl = formatUrl(url)
        return try {
            java.net.URL(formattedUrl)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 获取URL的协议部分
     */
    fun getProtocol(url: String): String {
        val formattedUrl = formatUrl(url)
        return try {
            val javaUrl = java.net.URL(formattedUrl)
            javaUrl.protocol
        } catch (e: Exception) {
            "https"
        }
    }

    /**
     * 获取URL的主机部分
     */
    fun getHost(url: String): String {
        val formattedUrl = formatUrl(url)
        return try {
            val javaUrl = java.net.URL(formattedUrl)
            javaUrl.host
        } catch (e: Exception) {
            url
        }
    }

    /**
     * 获取URL的端口部分
     */
    fun getPort(url: String): Int {
        val formattedUrl = formatUrl(url)
        return try {
            val javaUrl = java.net.URL(formattedUrl)
            javaUrl.port.takeIf { it > 0 } ?: javaUrl.defaultPort
        } catch (e: Exception) {
            443
        }
    }
}