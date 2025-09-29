package com.openlist.android.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openlist.android.data.api.AListApiService
import com.openlist.android.data.model.FileInfo
import com.openlist.android.data.model.FileListResponse
import com.openlist.android.data.network.NetworkModule
import com.openlist.android.data.network.NetworkConnectivityMonitor
import com.openlist.android.data.preferences.PreferencesRepository
import com.openlist.android.data.preferences.PreferencesRepositoryImpl
import com.openlist.android.data.preferences.DataStoreModule
import com.openlist.android.data.error.GlobalErrorHandler
import com.openlist.android.data.error.ErrorHandlingUtils
import com.openlist.android.data.error.NetworkException
import com.openlist.android.data.error.ValidationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras

class SimpleFileViewModel(
    private val application: Application
) : ViewModel() {

    private val _currentPath = MutableStateFlow("/")
    val currentPath: StateFlow<String> = _currentPath.asStateFlow()

    private val _files = MutableStateFlow<List<FileInfo>>(emptyList())
    val files: StateFlow<List<FileInfo>> = _files.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _selectedFiles = MutableStateFlow<List<FileInfo>>(emptyList())
    val selectedFiles: StateFlow<List<FileInfo>> = _selectedFiles.asStateFlow()

    // 全局错误处理器
    private val globalErrorHandler = GlobalErrorHandler.getInstance(application)

    // 网络连接监控器
    private val networkMonitor = NetworkConnectivityMonitor.getInstance(application)

    // 数据存储和网络组件
    private val preferencesRepository: PreferencesRepository by lazy {
        DataStoreModule.providePreferencesRepository(
            DataStoreModule.provideDataStore(application),
            application
        )
    }

    private val apiService: AListApiService by lazy {
        NetworkModule.provideAListApiService(
            NetworkModule.provideRetrofit(
                NetworkModule.provideOkHttpClient(NetworkModule.provideHttpLoggingInterceptor(), application, preferencesRepository),
                NetworkModule.provideGson(),
                preferencesRepository
            )
        )
    }

    init {
        loadFiles()
    }

    fun loadFiles(path: String = currentPath.value, refresh: Boolean = false) {
        viewModelScope.launch(globalErrorHandler.getCoroutineExceptionHandler()) {
            _isLoading.value = true
            _error.value = null

            try {
                // 检查网络连接
                if (!networkMonitor.isConnected.value) {
                    val networkInfo = networkMonitor.getCurrentNetworkInfo()
                    val errorMessage = when {
                        !networkInfo.hasInternet -> "设备未连接到网络"
                        !networkInfo.isValidated -> "网络连接已建立，但无法访问互联网"
                        else -> "网络连接失败，请检查网络设置"
                    }
                    _error.value = errorMessage
                    _isLoading.value = false
                    return@launch
                }

                val authToken = preferencesRepository.getAuthToken().first()
                if (authToken != null) {
                    // 验证文件路径
                    val pathValidation = ErrorHandlingUtils.validateFilePath(path)
                    if (pathValidation is ValidationResult.Error) {
                        _error.value = pathValidation.exception.message ?: "文件路径无效"
                        _isLoading.value = false
                        return@launch
                    }

                    // 使用安全API调用
                    ErrorHandlingUtils.safeApiCall(application) {
                        apiService.getFiles(
                            token = "Bearer $authToken",
                            path = path,
                            refresh = refresh
                        )
                    }.collect { result ->
                        result.fold(
                            onSuccess = { response ->
                                if (response.isSuccessful) {
                                    val fileListResponse = response.body()
                                    if (fileListResponse != null) {
                                        _files.value = fileListResponse.data.content
                                        _currentPath.value = fileListResponse.data.path
                                    } else {
                                        _error.value = "文件列表响应为空"
                                    }
                                } else {
                                    _error.value = globalErrorHandler.getApiErrorMessage(response.code())
                                }
                            },
                            onFailure = { exception ->
                                when (exception) {
                                    is NetworkException -> _error.value = globalErrorHandler.getNetworkErrorMessage(exception)
                                    else -> _error.value = "加载文件失败: ${exception.message}"
                                }
                            }
                        )
                    }
                } else {
                    _error.value = "请先登录"
                }
            } catch (e: Exception) {
                globalErrorHandler.handleError(e)
                _error.value = "加载文件失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchFiles(keywords: String, scope: Int = 0) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val authToken = preferencesRepository.getAuthToken().first()
                if (authToken != null && keywords.isNotBlank()) {
                    // 执行搜索API调用
                    val response = apiService.searchFiles(
                        token = "Bearer $authToken",
                        parent = currentPath.value,
                        keywords = keywords,
                        scope = scope
                    )

                    if (response.isSuccessful) {
                        val searchResponse = response.body()
                        if (searchResponse != null) {
                            _files.value = searchResponse.data.content
                        } else {
                            _error.value = "搜索响应为空"
                        }
                    } else {
                        _error.value = "搜索失败 (${response.code()})"
                    }
                } else {
                    _error.value = if (keywords.isBlank()) "请输入搜索关键词" else "请先登录"
                }
            } catch (e: Exception) {
                _error.value = "搜索失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createDirectory(path: String, name: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val authToken = preferencesRepository.getAuthToken().first()
                if (authToken != null) {
                    val fullPath = if (path.endsWith("/")) "$path$name" else "$path/$name"
                    val response = apiService.createDirectory(
                        token = "Bearer $authToken",
                        path = fullPath
                    )

                    if (response.isSuccessful) {
                        // 创建成功，刷新文件列表
                        loadFiles(path)
                    } else {
                        _error.value = "创建文件夹失败 (${response.code()})"
                    }
                } else {
                    _error.value = "请先登录"
                }
            } catch (e: Exception) {
                _error.value = "创建文件夹失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteFile(path: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val authToken = preferencesRepository.getAuthToken().first()
                if (authToken != null) {
                    val response = apiService.deleteFile(
                        token = "Bearer $authToken",
                        path = path
                    )

                    if (response.isSuccessful) {
                        // 删除成功，刷新文件列表
                        loadFiles(currentPath.value)
                    } else {
                        _error.value = "删除文件失败 (${response.code()})"
                    }
                } else {
                    _error.value = "请先登录"
                }
            } catch (e: Exception) {
                _error.value = "删除文件失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getFileLink(path: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val authToken = preferencesRepository.getAuthToken().first()
                if (authToken != null) {
                    val response = apiService.getFileLink(
                        token = "Bearer $authToken",
                        path = path
                    )

                    if (response.isSuccessful) {
                        val linkResponse = response.body()
                        onResult(linkResponse?.data?.url)
                    } else {
                        _error.value = "获取文件链接失败 (${response.code()})"
                        onResult(null)
                    }
                } else {
                    _error.value = "请先登录"
                    onResult(null)
                }
            } catch (e: Exception) {
                _error.value = "获取文件链接失败: ${e.message}"
                onResult(null)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun navigateToParent() {
        val current = currentPath.value
        if (current != "/") {
            val parentPath = current.substring(0, current.lastIndexOf('/').takeIf { it > 0 } ?: 0)
            loadFiles(if (parentPath.isEmpty()) "/" else parentPath)
        }
    }

    fun navigateToFolder(folder: FileInfo) {
        if (folder.is_dir) {
            loadFiles(folder.path)
        }
    }

    fun refresh() {
        loadFiles(currentPath.value, refresh = true)
    }

    fun selectFile(file: FileInfo) {
        _selectedFiles.value = _selectedFiles.value.toMutableList().apply {
            if (contains(file)) {
                remove(file)
            } else {
                add(file)
            }
        }
    }

    fun clearSelection() {
        _selectedFiles.value = emptyList()
    }

    fun selectAll() {
        _selectedFiles.value = _files.value
    }

    fun getCurrentPathSegments(): List<String> {
        return currentPath.value
            .split("/")
            .filter { it.isNotEmpty() }
            .toMutableList()
            .apply { add(0, "根目录") }
    }

    fun clearError() {
        _error.value = null
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as android.app.Application
                return SimpleFileViewModel(application) as T
            }
        }
    }
}