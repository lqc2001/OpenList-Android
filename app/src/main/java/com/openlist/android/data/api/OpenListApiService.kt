package com.openlist.android.data.api

import com.openlist.android.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface OpenListApiService {

    // 认证相关
    @POST("/api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @GET("/api/auth/me")
    suspend fun getCurrentUser(
        @Header("Authorization") token: String
    ): Response<User>

    @POST("/api/auth/logout")
    suspend fun logout(
        @Header("Authorization") token: String
    ): Response<ApiResponse>

    // 文件系统相关
    @GET("/api/fs/list")
    suspend fun getFiles(
        @Header("Authorization") token: String,
        @Query("path") path: String = "/",
        @Query("refresh") refresh: Boolean = false,
        @Query("password") password: String? = null
    ): Response<FileListResponse>

    @GET("/api/fs/get")
    suspend fun getFileInfo(
        @Header("Authorization") token: String,
        @Query("path") path: String,
        @Query("password") password: String? = null
    ): Response<FileInfo>

    @POST("/api/fs/mkdir")
    suspend fun createDirectory(
        @Header("Authorization") token: String,
        @Query("path") path: String
    ): Response<ApiResponse>

    @PUT("/api/fs/rename")
    suspend fun renameFile(
        @Header("Authorization") token: String,
        @Query("path") path: String,
        @Query("name") name: String
    ): Response<ApiResponse>

    @DELETE("/api/fs/remove")
    suspend fun deleteFile(
        @Header("Authorization") token: String,
        @Query("path") path: String
    ): Response<ApiResponse>

    // 文件上传/下载
    @POST("/api/fs/form")
    suspend fun uploadFile(
        @Header("Authorization") token: String,
        @Query("path") path: String,
        @Query("password") password: String? = null
    ): Response<UploadResponse>

    @GET("/api/fs/link")
    suspend fun getFileLink(
        @Header("Authorization") token: String,
        @Query("path") path: String,
        @Query("password") password: String? = null
    ): Response<FileLinkResponse>

    // 搜索功能
    @GET("/api/fs/search")
    suspend fun searchFiles(
        @Header("Authorization") token: String,
        @Query("parent") parent: String = "/",
        @Query("keywords") keywords: String,
        @Query("scope") scope: Int = 0 // 0: 当前目录, 1: 全局
    ): Response<SearchResponse>

    // 存储管理
    @GET("/api/admin/storage/list")
    suspend fun getStorageList(
        @Header("Authorization") token: String
    ): Response<StorageListResponse>

    // 任务管理
    @GET("/api/admin/task/finish")
    suspend fun getFinishedTasks(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): Response<TaskListResponse>

    @GET("/api/admin/task/unfinished")
    suspend fun getUnfinishedTasks(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): Response<TaskListResponse>
}