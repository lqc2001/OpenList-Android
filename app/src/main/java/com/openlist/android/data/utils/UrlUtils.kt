package com.openlist.android.data.utils

import android.util.Log
import java.util.regex.Pattern

object UrlUtils {

    private const val TAG = "UrlUtils"

    private val URL_PATTERN = Pattern.compile(
        "^https?://.+",
        Pattern.CASE_INSENSITIVE
    )

    // 更严格的URL验证模式
    private val STRICT_URL_PATTERN = Pattern.compile(
        "^https?://(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}(?::[0-9]+)?(?:/.*)?$",
        Pattern.CASE_INSENSITIVE
    )

    // IP地址模式
    private val IP_URL_PATTERN = Pattern.compile(
        "^https?://(?:[0-9]{1,3}\\.){3}[0-9]{1,3}(?::[0-9]+)?(?:/.*)?$",
        Pattern.CASE_INSENSITIVE
    )

    // 本地地址模式
    private val LOCALHOST_PATTERN = Pattern.compile(
        "^https?://localhost(?::[0-9]+)?(?:/.*)?$",
        Pattern.CASE_INSENSITIVE
    )

    /**
     * 格式化URL，确保包含协议前缀并进行优化处理
     */
    fun formatUrl(inputUrl: String): String? {
        val originalUrl = inputUrl.trim()
        var url = originalUrl

        Log.d(TAG, "=== Formatting URL ===")
        Log.d(TAG, "Input URL: '$originalUrl'")
        Log.d(TAG, "Trimmed URL: '$url'")

        // 如果为空，返回null
        if (url.isEmpty()) {
            Log.w(TAG, "URL is empty, returning null")
            return null
        }

        // 移除多余的空格和特殊字符
        val beforeSpaceRemoval = url
        url = url.replace("\\s+".toRegex(), "")
        if (beforeSpaceRemoval != url) {
            Log.d(TAG, "Removed whitespace: '$beforeSpaceRemoval' -> '$url'")
        }

        // 如果已经包含协议，验证并返回
        if (URL_PATTERN.matcher(url).matches()) {
            Log.d(TAG, "URL already has protocol: $url")
            Log.d(TAG, "Protocol detected, proceeding to optimization...")
            return optimizeUrl(url)
        }

        // 检查URL类型
        Log.d(TAG, "URL has no protocol, analyzing type...")
        val isIp = isIpAddress(url)
        val isDomain = isDomainName(url)
        val isLocal = isLocalAddress(url)

        Log.d(TAG, "URL analysis results:")
        Log.d(TAG, "  - isIpAddress: $isIp")
        Log.d(TAG, "  - isDomainName: $isDomain")
        Log.d(TAG, "  - isLocalAddress: $isLocal")

        return when {
            isIp || isDomain -> {
                val formattedUrl = "http://$url"
                Log.d(TAG, "Formatted IP/domain URL: $formattedUrl")
                Log.d(TAG, "Proceeding to optimization...")
                optimizeUrl(formattedUrl)
            }
            isLocal -> {
                val formattedUrl = "http://$url"
                Log.d(TAG, "Formatted local URL: $formattedUrl")
                Log.d(TAG, "Proceeding to optimization...")
                optimizeUrl(formattedUrl)
            }
            else -> {
                Log.w(TAG, "Invalid URL format: $url")
                Log.w(TAG, "URL doesn't match any known pattern (IP, domain, or local)")
                null
            }
        }
    }

    /**
     * 优化URL，应用特定的修复规则
     */
    private fun optimizeUrl(url: String): String {
        val originalUrl = url
        var optimizedUrl = url

        Log.d(TAG, "=== Optimizing URL ===")
        Log.d(TAG, "Input URL: $originalUrl")

        // 修复DNS解析问题 - 如果是特定域名，直接使用正确的IP
        val beforeDnsFix = optimizedUrl
        optimizedUrl = when {
            optimizedUrl.contains("j.yzfycz.cn") -> {
                Log.d(TAG, "Applying DNS fix for j.yzfycz.cn domain")
                optimizedUrl.replace("j.yzfycz.cn", "140.250.217.58")
            }
            optimizedUrl.contains("yzfycz.cn") -> {
                Log.d(TAG, "Applying DNS fix for yzfycz.cn domain")
                optimizedUrl.replace("yzfycz.cn", "140.250.217.58")
            }
            else -> {
                Log.d(TAG, "No DNS fix needed for this URL")
                optimizedUrl
            }
        }
        if (beforeDnsFix != optimizedUrl) {
            Log.d(TAG, "DNS fix applied: '$beforeDnsFix' -> '$optimizedUrl'")
        }

        // 强制使用HTTP协议（避免HTTPS证书问题）
        if (optimizedUrl.startsWith("https://")) {
            val beforeProtocolChange = optimizedUrl
            optimizedUrl = optimizedUrl.replace("https://", "http://")
            Log.d(TAG, "Forced HTTP protocol: '$beforeProtocolChange' -> '$optimizedUrl'")
        } else {
            Log.d(TAG, "URL already uses HTTP protocol, no change needed")
        }

        // 移除末尾的斜杠
        val beforeSlashRemoval = optimizedUrl
        optimizedUrl = optimizedUrl.replace("/+$".toRegex(), "")
        if (beforeSlashRemoval != optimizedUrl) {
            Log.d(TAG, "Removed trailing slashes: '$beforeSlashRemoval' -> '$optimizedUrl'")
        }

        Log.d(TAG, "URL optimization completed")
        Log.d(TAG, "Final optimized URL: $optimizedUrl")
        return optimizedUrl
    }

    /**
     * 验证URL格式
     */
    fun isValidUrl(url: String): Boolean {
        Log.d(TAG, "=== Validating URL ===")
        Log.d(TAG, "Input URL: '$url'")

        if (url.isEmpty()) {
            Log.w(TAG, "URL validation failed: empty URL")
            Log.d(TAG, "Validation result: false (empty URL)")
            return false
        }

        Log.d(TAG, "Formatting URL for validation...")
        val formattedUrl = formatUrl(url)
        if (formattedUrl == null) {
            Log.w(TAG, "URL validation failed: formatting returned null")
            Log.d(TAG, "Validation result: false (formatting failed)")
            return false
        }

        Log.d(TAG, "Formatted URL: '$formattedUrl'")
        Log.d(TAG, "Performing pattern matching validation...")

        return try {
            val javaUrl = java.net.URL(formattedUrl)
            Log.d(TAG, "Java URL object created successfully")

            val strictMatch = STRICT_URL_PATTERN.matcher(formattedUrl).matches()
            val ipMatch = IP_URL_PATTERN.matcher(formattedUrl).matches()
            val localMatch = LOCALHOST_PATTERN.matcher(formattedUrl).matches()

            Log.d(TAG, "Pattern matching results:")
            Log.d(TAG, "  - Strict URL pattern: $strictMatch")
            Log.d(TAG, "  - IP URL pattern: $ipMatch")
            Log.d(TAG, "  - Localhost pattern: $localMatch")

            val isValid = strictMatch || ipMatch || localMatch

            Log.d(TAG, "URL validation result: $isValid for '$formattedUrl'")
            Log.d(TAG, "Validation completed successfully")
            isValid
        } catch (e: Exception) {
            Log.e(TAG, "URL validation exception", e)
            Log.e(TAG, "Exception type: ${e.javaClass.simpleName}")
            Log.e(TAG, "Exception message: ${e.message}")
            Log.d(TAG, "Validation result: false (exception occurred)")
            false
        }
    }

    /**
     * 检查是否是IP地址
     */
    private fun isIpAddress(address: String): Boolean {
        val ipPattern = Pattern.compile(
            "^([0-9]{1,3}\\.){3}[0-9]{1,3}(:[0-9]+)?$"
        )
        return ipPattern.matcher(address).matches()
    }

    /**
     * 检查是否是域名
     */
    private fun isDomainName(address: String): Boolean {
        val domainPattern = Pattern.compile(
            "^[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*(\\.[a-zA-Z]{2,})(:[0-9]+)?$"
        )
        return domainPattern.matcher(address).matches()
    }

    /**
     * 检查是否是本地地址
     */
    private fun isLocalAddress(address: String): Boolean {
        val localPattern = Pattern.compile(
            "^localhost(:[0-9]+)?$"
        )
        return localPattern.matcher(address).matches()
    }

    /**
     * 获取URL的协议部分
     */
    fun getProtocol(url: String): String {
        val formattedUrl = formatUrl(url) ?: return "http"
        return try {
            val javaUrl = java.net.URL(formattedUrl)
            javaUrl.protocol
        } catch (e: Exception) {
            Log.e(TAG, "Error getting protocol", e)
            "http"
        }
    }

    /**
     * 获取URL的主机部分
     */
    fun getHost(url: String): String {
        val formattedUrl = formatUrl(url) ?: return url
        return try {
            val javaUrl = java.net.URL(formattedUrl)
            javaUrl.host
        } catch (e: Exception) {
            Log.e(TAG, "Error getting host", e)
            url
        }
    }

    /**
     * 获取URL的端口部分
     */
    fun getPort(url: String): Int {
        val formattedUrl = formatUrl(url) ?: return 5244
        return try {
            val javaUrl = java.net.URL(formattedUrl)
            javaUrl.port.takeIf { it > 0 } ?: 5244
        } catch (e: Exception) {
            Log.e(TAG, "Error getting port", e)
            5244
        }
    }

    /**
     * 验证并格式化URL，返回格式化后的URL或错误信息
     */
    fun validateAndFormat(inputUrl: String): Pair<String?, String?> {
        Log.d(TAG, "Validating and formatting URL: $inputUrl")

        if (inputUrl.trim().isEmpty()) {
            return Pair(null, "URL不能为空")
        }

        val formattedUrl = formatUrl(inputUrl)
        if (formattedUrl == null) {
            return Pair(null, "URL格式无效，请输入有效的服务器地址")
        }

        if (!isValidUrl(formattedUrl)) {
            return Pair(null, "URL格式无效，请检查服务器地址")
        }

        // 验证端口范围
        try {
            val port = getPort(formattedUrl)
            if (port !in 1..65535) {
                return Pair(null, "端口号必须在1-65535之间")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Port validation error", e)
            return Pair(null, "端口号无效")
        }

        Log.d(TAG, "URL validation successful: $formattedUrl")
        return Pair(formattedUrl, null)
    }

    /**
     * 获取URL错误信息
     */
    fun getErrorMessage(url: String): String? {
        val (_, error) = validateAndFormat(url)
        return error
    }

    /**
     * 检查URL是否需要格式化建议
     */
    fun needsFormattingSuggestion(url: String): Boolean {
        if (url.trim().isEmpty()) return false
        if (URL_PATTERN.matcher(url).matches()) return false

        return isIpAddress(url) || isDomainName(url) || isLocalAddress(url)
    }

    /**
     * 生成格式化建议
     */
    fun generateFormattingSuggestions(url: String): List<String> {
        val suggestions = mutableListOf<String>()
        val cleanUrl = url.trim()

        if (cleanUrl.isEmpty()) return suggestions

        if (!URL_PATTERN.matcher(cleanUrl).matches()) {
            suggestions.add("http://$cleanUrl")
            suggestions.add("https://$cleanUrl")
        }

        return suggestions
    }
}