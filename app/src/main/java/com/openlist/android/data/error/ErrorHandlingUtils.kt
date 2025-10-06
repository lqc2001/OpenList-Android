package com.openlist.android.data.error

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException

object ErrorHandlingUtils {

    inline fun <T> safeApiCall(
        context: Context,
        crossinline apiCall: suspend () -> T
    ): Flow<Result<T>> = flow {
        try {
            val result = apiCall()
            emit(Result.success(result))
        } catch (e: Exception) {
            val globalErrorHandler = GlobalErrorHandler.getInstance(context)
            val handledException = handleApiException(e)
            globalErrorHandler.handleError(handledException)
            emit(Result.failure(handledException))
        }
    }

    fun handleApiException(exception: Throwable): Exception {
        return when (exception) {
            is HttpException -> {
                when (exception.code()) {
                    401 -> AuthenticationException("认证令牌无效或已过期", exception)
                    403 -> AuthenticationException("访问被拒绝，权限不足", exception)
                    404 -> ServerException("请求的资源不存在", exception.code(), exception)
                    500 -> ServerException("服务器内部错误", exception.code(), exception)
                    else -> NetworkException(
                        "API请求失败: ${exception.code()} ${exception.message()}",
                        exception.code(),
                        exception
                    )
                }
            }
            is IOException -> {
                when (exception) {
                    is java.net.SocketTimeoutException ->
                        NetworkException("连接超时", null, exception)
                    is java.net.UnknownHostException ->
                        NetworkException("无法连接到服务器", null, exception)
                    is javax.net.ssl.SSLException ->
                        NetworkException("SSL连接错误", null, exception)
                    else ->
                        NetworkException("网络连接失败", null, exception)
                }
            }
            is SecurityException -> {
                DataStorageException("数据访问安全异常", exception)
            }
            else -> {
                OpenListException("未知错误: ${exception.message}", exception)
            }
        }
    }

    
    inline fun <T> executeSafely(
        context: Context,
        crossinline operation: () -> T,
        crossinline onError: (Exception) -> Unit = {}
    ): Result<T> {
        return try {
            Result.success(operation())
        } catch (e: Exception) {
            val globalErrorHandler = GlobalErrorHandler.getInstance(context)
            val handledException = handleApiException(e)
            globalErrorHandler.handleError(handledException)
            onError(handledException)
            Result.failure(handledException)
        }
    }

    fun validateRequiredFields(
        fields: Map<String, String?>
    ): ValidationResult {
        val missingFields = fields.filter { it.value.isNullOrBlank() }

        if (missingFields.isNotEmpty()) {
            return ValidationResult.Error(
                ValidationException(
                    "以下字段为必填项: ${missingFields.keys.joinToString()}",
                    missingFields.keys.firstOrNull()
                )
            )
        }

        return ValidationResult.Success
    }

    fun validateUrl(url: String?): ValidationResult {
        if (url.isNullOrBlank()) {
            return ValidationResult.Error(
                ValidationException("URL不能为空", "url")
            )
        }

        if (!url.matches(Regex("^(https?|ftp)://[^\\s/$.?#].[^\\s]*$"))) {
            return ValidationResult.Error(
                ValidationException("URL格式无效", "url")
            )
        }

        return ValidationResult.Success
    }

    fun validateFilePath(path: String?): ValidationResult {
        if (path.isNullOrBlank()) {
            return ValidationResult.Error(
                ValidationException("文件路径不能为空", "path")
            )
        }

        if (path.contains("..")) {
            return ValidationResult.Error(
                ValidationException("文件路径包含非法字符", "path")
            )
        }

        return ValidationResult.Success
    }
}

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val exception: ValidationException) : ValidationResult()
}