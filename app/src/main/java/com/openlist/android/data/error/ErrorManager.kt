package com.openlist.android.data.error

import android.content.Context
import android.util.Log
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.ui.res.stringResource
import com.openlist.android.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * 错误管理器 - 处理应用中的错误显示和重试逻辑
 */
class ErrorManager private constructor(private val context: Context) {

    companion object {
        private const val TAG = "ErrorManager"
        private const val MAX_QUEUE_SIZE = 5
        private const val MIN_ERROR_INTERVAL = 3000L // 3秒
        private const val SAME_ERROR_INTERVAL = 10000L // 10秒

        @Volatile
        private var instance: ErrorManager? = null

        fun getInstance(context: Context): ErrorManager {
            return instance ?: synchronized(this) {
                instance ?: ErrorManager(context.applicationContext).also { instance = it }
            }
        }
    }

    // 错误队列
    private val _errorQueue = MutableStateFlow<List<ErrorItem>>(emptyList())
    val errorQueue: StateFlow<List<ErrorItem>> = _errorQueue.asStateFlow()

    // 最后显示错误的时间
    private val lastErrorTimes = ConcurrentHashMap<String, Long>()

    // 当前显示的Snackbar
    private var currentSnackbar: androidx.compose.material3.SnackbarHostState? = null

    /**
     * 错误数据类
     */
    data class ErrorItem(
        val id: String,
        val message: String,
        val priority: ErrorPriority,
        val timestamp: Long,
        val retryAction: (() -> Unit)? = null,
        var hasBeenShown: Boolean = false
    )

    /**
     * 错误优先级
     */
    enum class ErrorPriority(val value: Int) {
        CRITICAL(5),    // 认证错误、致命错误
        HIGH(4),        // 网络错误、连接错误
        MEDIUM(3),      // 服务器错误、超时错误
        LOW(2),         // 数据解析错误
        INFO(1)         // 信息性提示
    }

    /**
     * 添加错误到队列
     */
    fun addError(
        message: String,
        priority: ErrorPriority = ErrorPriority.MEDIUM,
        retryAction: (() -> Unit)? = null,
        type: String = "default"
    ) {
        try {
            Log.d(TAG, "Adding error: $message, priority: $priority, type: $type")

            val currentTime = System.currentTimeMillis()
            val errorId = "${type}_${priority}_${message.hashCode()}_${currentTime}"

            // 检查是否频繁显示相同错误
            val lastTime = lastErrorTimes[errorId]
            if (lastTime != null && currentTime - lastTime < SAME_ERROR_INTERVAL) {
                Log.d(TAG, "Error $errorId shown recently, skipping")
                return
            }

            // 检查队列大小
            val currentQueue = _errorQueue.value.toMutableList()
            if (currentQueue.size >= MAX_QUEUE_SIZE) {
                // 移除最低优先级的错误
                val removed = currentQueue.minByOrNull { it.priority.value }
                removed?.let { currentQueue.remove(it) }
                Log.d(TAG, "Error queue full, removed lowest priority error: ${removed?.message}")
            }

            // 创建新错误项
            val errorItem = ErrorItem(
                id = errorId,
                message = message,
                priority = priority,
                timestamp = currentTime,
                retryAction = retryAction
            )

            // 按优先级插入错误
            val insertIndex = currentQueue.indexOfFirst { it.priority.value < priority.value }
                .takeIf { it != -1 } ?: currentQueue.size

            currentQueue.add(insertIndex, errorItem)
            _errorQueue.value = currentQueue

            Log.d(TAG, "Error added to queue at position $insertIndex. Queue size: ${currentQueue.size}")

        } catch (e: Exception) {
            Log.e(TAG, "Error adding error to queue", e)
        }
    }

    /**
     * 获取下一个要显示的错误
     */
    fun getNextError(): ErrorItem? {
        try {
            val currentTime = System.currentTimeMillis()
            val queue = _errorQueue.value

            // 找到下一个未显示且满足时间间隔的错误
            val nextError = queue.firstOrNull { error ->
                !error.hasBeenShown && (currentTime - error.timestamp) >= MIN_ERROR_INTERVAL
            }

            nextError?.let { error ->
                // 标记为已显示
                error.hasBeenShown = true
                lastErrorTimes[error.id] = currentTime

                Log.d(TAG, "Showing error: ${error.message}")
                return error
            }

            return null
        } catch (e: Exception) {
            Log.e(TAG, "Error getting next error", e)
            return null
        }
    }

    /**
     * 移除已显示的错误
     */
    fun removeShownErrors() {
        try {
            val currentQueue = _errorQueue.value.toMutableList()
            val shownErrors = currentQueue.filter { it.hasBeenShown }

            currentQueue.removeAll(shownErrors)
            _errorQueue.value = currentQueue

            Log.d(TAG, "Removed ${shownErrors.size} shown errors from queue. Remaining: ${currentQueue.size}")
        } catch (e: Exception) {
            Log.e(TAG, "Error removing shown errors", e)
        }
    }

    /**
     * 清空错误队列
     */
    fun clearErrors() {
        Log.d(TAG, "Clearing error queue")
        _errorQueue.value = emptyList()
        lastErrorTimes.clear()
    }

    /**
     * 设置Snackbar主机状态
     */
    fun setSnackbarHostState(snackbarHostState: androidx.compose.material3.SnackbarHostState) {
        currentSnackbar = snackbarHostState
    }

    /**
     * 显示错误提示
     */
    suspend fun showError(
        message: String,
        priority: ErrorPriority = ErrorPriority.MEDIUM,
        retryAction: (() -> Unit)? = null,
        type: String = "default"
    ) {
        try {
            addError(message, priority, retryAction, type)

            val snackbarHostState = currentSnackbar
            if (snackbarHostState != null) {
                showNextError(snackbarHostState)
            } else {
                Log.w(TAG, "SnackbarHostState not set, error queued for later display")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing error", e)
        }
    }

    /**
     * 显示下一个错误
     */
    private suspend fun showNextError(snackbarHostState: androidx.compose.material3.SnackbarHostState) {
        try {
            val nextError = getNextError() ?: return

            val result = snackbarHostState.showSnackbar(
                message = nextError.message,
                actionLabel = if (nextError.retryAction != null) "重试" else null,
                duration = SnackbarDuration.Indefinite,
                withDismissAction = true
            )

            when (result) {
                SnackbarResult.ActionPerformed -> {
                    Log.d(TAG, "User clicked retry for error: ${nextError.message}")
                    nextError.retryAction?.invoke()
                }
                SnackbarResult.Dismissed -> {
                    Log.d(TAG, "User dismissed error: ${nextError.message}")
                }
            }

            // 移除已显示的错误
            removeShownErrors()

            // 继续显示下一个错误
            showNextError(snackbarHostState)

        } catch (e: Exception) {
            Log.e(TAG, "Error showing next error in snackbar", e)
        }
    }

    /**
     * 根据异常类型添加错误
     */
    fun addException(exception: Throwable, retryAction: (() -> Unit)? = null) {
        try {
            val (message, priority) = when (exception) {
                is AuthenticationException ->
                    "用户名或密码错误" to ErrorPriority.CRITICAL
                is NetworkException ->
                    "网络错误: ${exception.message}" to ErrorPriority.HIGH
                is java.net.SocketTimeoutException ->
                    "连接超时，请检查网络连接" to ErrorPriority.MEDIUM
                is java.net.ConnectException ->
                    "无法连接到服务器" to ErrorPriority.HIGH
                is org.json.JSONException ->
                    "数据解析错误" to ErrorPriority.LOW
                else ->
                    "操作失败: ${exception.message}" to ErrorPriority.MEDIUM
            }

            addError(message, priority, retryAction, exception.javaClass.simpleName)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding exception", e)
        }
    }

    /**
     * 获取错误队列状态
     */
    fun getErrorStats(): Map<String, Any> {
        return mapOf(
            "queue_size" to _errorQueue.value.size,
            "shown_count" to _errorQueue.value.count { it.hasBeenShown },
            "pending_count" to _errorQueue.value.count { !it.hasBeenShown },
            "priorities" to _errorQueue.value.groupingBy { it.priority.name }.eachCount()
        )
    }
}