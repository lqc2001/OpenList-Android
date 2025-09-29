package com.alist.android.data.model

data class ApiResponse(
    val code: Int,
    val message: String,
    val data: Any?
)

data class StorageListResponse(
    val code: Int,
    val message: String,
    val data: List<StorageInfo>
)

data class StorageInfo(
    val id: Int,
    val mount_path: String,
    val order: Int,
    val driver: String,
    val cache_expiration: Int,
    val status: String,
    val addition: String,
    val remark: String,
    val modified: String,
    val disabled: Boolean,
    val enable_sign: Boolean,
    val order_by: String,
    val order_direction: String,
    val extract_folder: String,
    val web_proxy: Boolean,
    val webdav_policy: String,
    val down_proxy_url: String,
    val proxy_range: Boolean
)

data class TaskListResponse(
    val code: Int,
    val message: String,
    val data: TaskListData
)

data class TaskListData(
    val content: List<TaskInfo>,
    val total: Int,
    val page: Int,
    val per_page: Int
)

data class TaskInfo(
    val id: String,
    val name: String,
    val type: String,
    val status: String,
    val progress: Int,
    val created_at: String,
    val updated_at: String,
    val error: String?,
    val result: Any?
)