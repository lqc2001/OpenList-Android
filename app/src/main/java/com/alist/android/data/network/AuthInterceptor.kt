package com.alist.android.data.network

import com.alist.android.data.preferences.PreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val preferencesRepository: PreferencesRepository
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // 获取认证令牌
        val authToken = runBlocking {
            preferencesRepository.getAuthToken().first()
        }

        return if (authToken != null && authToken.isNotEmpty()) {
            // 添加认证头
            val authenticatedRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $authToken")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("User-Agent", "AList-Android/1.0.0")
                .build()

            chain.proceed(authenticatedRequest)
        } else {
            // 对于不需要认证的请求，直接发送
            val publicRequest = originalRequest.newBuilder()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("User-Agent", "AList-Android/1.0.0")
                .build()

            chain.proceed(publicRequest)
        }
    }
}