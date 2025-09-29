package com.openlist.android.data.network

import android.app.Application
import com.openlist.android.data.api.AListApiService
import com.openlist.android.data.preferences.PreferencesRepository
import com.openlist.android.data.utils.UrlUtils
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {

    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }

    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        application: Application,
        preferencesRepository: PreferencesRepository
    ): OkHttpClient {
        val networkMonitor = NetworkConnectivityMonitor.getInstance(application)

        return ConnectionPoolConfig.configureOptimizations(
            OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                // 网络状态检测拦截器
                .addInterceptor(NetworkStateInterceptor(application, networkMonitor))
                // 重试拦截器
                .addInterceptor(RetryInterceptor(maxRetries = 3, retryDelay = 1000))
                // 认证拦截器
                .addInterceptor(AuthInterceptor(preferencesRepository))
        ).build()
    }

    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson,
        preferencesRepository: PreferencesRepository
    ): Retrofit {
        // 获取服务器URL，默认使用空值，在实际使用时动态设置
        val savedUrl = runBlocking {
            preferencesRepository.getServerUrl().first()
        }

        // 格式化URL确保包含协议前缀
        val baseUrl = if (savedUrl.isNullOrEmpty()) {
            "http://localhost:5244"
        } else {
            UrlUtils.formatUrl(savedUrl)
        }

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    fun provideAListApiService(
        retrofit: Retrofit
    ): AListApiService {
        return retrofit.create(AListApiService::class.java)
    }
}