package com.openlist.android.data.repository

import com.openlist.android.data.api.OpenListApiService
import com.openlist.android.data.model.*
import com.openlist.android.data.preferences.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileRepository @Inject constructor(
    private val apiService: OpenListApiService,
    private val preferencesRepository: PreferencesRepository
) {

    suspend fun getFiles(
        path: String = "/",
        refresh: Boolean = false,
        password: String? = null
    ): Flow<Result<FileListResponse>> = flow {
        try {
            val token = preferencesRepository.getAuthToken().first()
            if (token.isNullOrEmpty()) {
                emit(Result.failure(Exception("未找到认证令牌")))
                return@flow
            }

            val response = apiService.getFiles("Bearer $token", path, refresh, password)
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!))
            } else {
                emit(Result.failure(Exception("获取文件列表失败: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun getFileInfo(
        path: String,
        password: String? = null
    ): Flow<Result<FileInfo>> = flow {
        try {
            val token = preferencesRepository.getAuthToken().first()
            if (token.isNullOrEmpty()) {
                emit(Result.failure(Exception("未找到认证令牌")))
                return@flow
            }

            val response = apiService.getFileInfo("Bearer $token", path, password)
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!))
            } else {
                emit(Result.failure(Exception("获取文件信息失败: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun createDirectory(path: String): Flow<Result<ApiResponse>> = flow {
        try {
            val token = preferencesRepository.getAuthToken().first()
            if (token.isNullOrEmpty()) {
                emit(Result.failure(Exception("未找到认证令牌")))
                return@flow
            }

            val response = apiService.createDirectory("Bearer $token", path)
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!))
            } else {
                emit(Result.failure(Exception("创建目录失败: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun renameFile(path: String, newName: String): Flow<Result<ApiResponse>> = flow {
        try {
            val token = preferencesRepository.getAuthToken().first()
            if (token.isNullOrEmpty()) {
                emit(Result.failure(Exception("未找到认证令牌")))
                return@flow
            }

            val response = apiService.renameFile("Bearer $token", path, newName)
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!))
            } else {
                emit(Result.failure(Exception("重命名文件失败: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun deleteFile(path: String): Flow<Result<ApiResponse>> = flow {
        try {
            val token = preferencesRepository.getAuthToken().first()
            if (token.isNullOrEmpty()) {
                emit(Result.failure(Exception("未找到认证令牌")))
                return@flow
            }

            val response = apiService.deleteFile("Bearer $token", path)
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!))
            } else {
                emit(Result.failure(Exception("删除文件失败: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun getFileLink(
        path: String,
        password: String? = null
    ): Flow<Result<FileLinkResponse>> = flow {
        try {
            val token = preferencesRepository.getAuthToken().first()
            if (token.isNullOrEmpty()) {
                emit(Result.failure(Exception("未找到认证令牌")))
                return@flow
            }

            val response = apiService.getFileLink("Bearer $token", path, password)
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!))
            } else {
                emit(Result.failure(Exception("获取文件链接失败: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun searchFiles(
        parent: String = "/",
        keywords: String,
        scope: Int = 0
    ): Flow<Result<SearchResponse>> = flow {
        try {
            val token = preferencesRepository.getAuthToken().first()
            if (token.isNullOrEmpty()) {
                emit(Result.failure(Exception("未找到认证令牌")))
                return@flow
            }

            val response = apiService.searchFiles("Bearer $token", parent, keywords, scope)
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!))
            } else {
                emit(Result.failure(Exception("搜索文件失败: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun getStorageList(): Flow<Result<StorageListResponse>> = flow {
        try {
            val token = preferencesRepository.getAuthToken().first()
            if (token.isNullOrEmpty()) {
                emit(Result.failure(Exception("未找到认证令牌")))
                return@flow
            }

            val response = apiService.getStorageList("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!))
            } else {
                emit(Result.failure(Exception("获取存储列表失败: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun uploadFile(
        path: String,
        password: String? = null
    ): Flow<Result<UploadResponse>> = flow {
        try {
            val token = preferencesRepository.getAuthToken().first()
            if (token.isNullOrEmpty()) {
                emit(Result.failure(Exception("未找到认证令牌")))
                return@flow
            }

            val response = apiService.uploadFile("Bearer $token", path, password)
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!))
            } else {
                emit(Result.failure(Exception("文件上传失败: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}