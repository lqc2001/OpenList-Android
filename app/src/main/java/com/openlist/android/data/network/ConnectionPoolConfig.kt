package com.openlist.android.data.network

import okhttp3.ConnectionPool
import okhttp3.Dns
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object ConnectionPoolConfig {

    fun createOptimizedConnectionPool(): ConnectionPool {
        return ConnectionPool(
            maxIdleConnections = 5,      // 最大空闲连接数
            keepAliveDuration = 5,        // 保持连接时间（分钟）
            timeUnit = TimeUnit.MINUTES
        )
    }

    fun configureOptimizations(builder: OkHttpClient.Builder): OkHttpClient.Builder {
        return builder
            // 连接池配置
            .connectionPool(createOptimizedConnectionPool())

            // 连接超时配置
            .connectTimeout(20, TimeUnit.SECONDS)      // 连接超时
            .readTimeout(60, TimeUnit.SECONDS)         // 读取超时（增加）
            .writeTimeout(60, TimeUnit.SECONDS)        // 写入超时（增加）
            .callTimeout(90, TimeUnit.SECONDS)         // 整个调用超时

            // 连接重用配置
            .retryOnConnectionFailure(true)           // 连接失败时重试

            // DNS配置
            .dns(Dns.SYSTEM)

            // 心跳检测
            .pingInterval(30, TimeUnit.SECONDS)         // 每30秒发送心跳包

            // 缓存配置
            .addNetworkInterceptor { chain ->
                val originalResponse = chain.proceed(chain.request())

                // 为成功的响应添加缓存头
                if (originalResponse.isSuccessful) {
                    val cacheControl = originalResponse.header("Cache-Control")
                    if (cacheControl == null || cacheControl.contains("no-store") || cacheControl.contains("no-cache") ||
                        cacheControl.contains("must-revalidate") || cacheControl.contains("max-age=0")) {
                        originalResponse.newBuilder()
                            .header("Cache-Control", "public, max-age=60") // 缓存1分钟
                            .build()
                    } else {
                        originalResponse
                    }
                } else {
                    originalResponse
                }
            }
    }
}