package com.openlist.android.data.network

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

class RetryInterceptor(
    private val maxRetries: Int = 3,
    private val retryDelay: Long = 1000 // 1秒延迟
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var lastResponse: Response? = null
        var exception: IOException? = null

        for (attempt in 0 until maxRetries) {
            try {
                val response = chain.proceed(request)

                // 如果响应成功，直接返回
                if (response.isSuccessful) {
                    return response
                }

                // 记录最后一次响应
                lastResponse = response

                // 对于特定的错误码，立即重试
                val shouldRetry = shouldRetryForStatusCode(response.code)
                if (shouldRetry && attempt < maxRetries - 1) {
                    response.close()
                    delayForRetry(attempt)
                    continue
                }

                return response
            } catch (e: IOException) {
                exception = e

                // 如果是最后一次尝试，抛出异常
                if (attempt == maxRetries - 1) {
                    throw e
                }

                // 延迟后重试
                delayForRetry(attempt)
            }
        }

        // 如果所有重试都失败了，返回最后一次响应或抛出异常
        return lastResponse ?: throw exception ?: IOException("Unknown network error")
    }

    private fun shouldRetryForStatusCode(statusCode: Int): Boolean {
        return when (statusCode) {
            408, // Request Timeout
            429, // Too Many Requests
            500, // Internal Server Error
            502, // Bad Gateway
            503, // Service Unavailable
            504  // Gateway Timeout
            -> true
            else -> false
        }
    }

    private fun delayForRetry(attempt: Int) {
        val delay = retryDelay * (attempt + 1) // 指数退避
        try {
            TimeUnit.MILLISECONDS.sleep(delay)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }
}