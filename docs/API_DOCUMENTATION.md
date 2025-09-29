# AList Android API 文档

## 📖 文档信息
- **API版本**: 基于 AList 3.0+
- **更新日期**: 2025年9月28日
- **认证方式**: Bearer Token
- **基础URL**: 用户配置的服务器地址

---

## 🔐 认证 API

### 登录认证

#### POST /api/auth/login
用户登录获取访问令牌

**请求参数**:
```json
{
  "username": "string",
  "password": "string"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "username": "admin",
      "password": "***",
      "base_path": "/",
      "role": 2,
      "disabled": false,
      "permission": 255,
      "sso_id": null,
      "sso_official": null
    }
  }
}
```

#### GET /api/auth/me
获取当前用户信息

**请求头**:
```
Authorization: Bearer <token>
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "admin",
    "base_path": "/",
    "role": 2,
    "disabled": false,
    "permission": 255
  }
}
```

#### POST /api/auth/logout
用户登出

**请求头**:
```
Authorization: Bearer <token>
```

**响应示例**:
```json
{
  "code": 200,
  "message": "logout success",
  "data": null
}
```

---

## 📁 文件系统 API

### 获取文件列表

#### GET /api/fs/list
获取指定路径下的文件列表

**请求参数**:
- `token` (header): Bearer token
- `path` (query): 文件路径，默认为 "/"
- `refresh` (query): 是否刷新缓存，默认为 false
- `password` (query): 目录密码（如有）

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "name": "Documents",
        "size": 0,
        "is_dir": true,
        "modified": "2025-09-28T10:30:00Z",
        "sign": "",
        "thumb": null,
        "type": 1,
        "hash_info": null,
        "hash_value": null,
        "path": "/Documents",
        "parent": "/",
        "proxy_url": null,
        "readme": null,
        "provider": "Local",
        "related_files": null
      }
    ],
    "total": 1,
    "path": "/",
    "readme": null,
    "write": true,
    "provider": "Local"
  }
}
```

### 获取文件信息

#### GET /api/fs/get
获取文件详细信息

**请求参数**:
- `token` (header): Bearer token
- `path` (query): 文件路径
- `password` (query): 文件密码（如有）

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "name": "example.pdf",
    "size": 1024000,
    "is_dir": false,
    "modified": "2025-09-28T10:30:00Z",
    "sign": "abc123",
    "thumb": null,
    "type": 0,
    "hash_info": "md5",
    "hash_value": "d41d8cd98f00b204e9800998ecf8427e",
    "path": "/example.pdf",
    "parent": "/",
    "proxy_url": null,
    "readme": null,
    "provider": "Local",
    "related_files": null
  }
}
```

### 创建目录

#### POST /api/fs/mkdir
创建新目录

**请求参数**:
- `token` (header): Bearer token
- `path` (query): 新目录路径

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 重命名文件

#### PUT /api/fs/rename
重命名文件或目录

**请求参数**:
- `token` (header): Bearer token
- `path` (query): 文件当前路径
- `name` (query): 新文件名

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 删除文件

#### DELETE /api/fs/remove
删除文件或目录

**请求参数**:
- `token` (header): Bearer token
- `path` (query): 文件路径

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

## 🔗 文件链接 API

### 获取文件链接

#### GET /api/fs/link
获取文件直接下载或访问链接

**请求参数**:
- `token` (header): Bearer token
- `path` (query): 文件路径
- `password` (query): 文件密码（如有）

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "raw_url": "https://example.com/raw/example.pdf",
    "url": "https://example.com/d/example.pdf",
    "task_url": null
  }
}
```

### 文件上传

#### POST /api/fs/form
获取文件上传信息

**请求参数**:
- `token` (header): Bearer token
- `path` (query): 上传目标路径
- `password` (query): 目录密码（如有）

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "upload_url": "https://example.com/upload",
    "callback_url": "https://example.com/callback"
  }
}
```

---

## 🔍 搜索 API

### 搜索文件

#### GET /api/fs/search
搜索文件

**请求参数**:
- `token` (header): Bearer token
- `parent` (query): 搜索父目录，默认为 "/"
- `keywords` (query): 搜索关键词
- `scope` (query): 搜索范围，0=当前目录，1=全局搜索

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "name": "example.pdf",
        "size": 1024000,
        "is_dir": false,
        "modified": "2025-09-28T10:30:00Z",
        "sign": "abc123",
        "thumb": null,
        "type": 0,
        "hash_info": "md5",
        "hash_value": "d41d8cd98f00b204e9800998ecf8427e",
        "path": "/example.pdf",
        "parent": "/",
        "proxy_url": null,
        "readme": null,
        "provider": "Local",
        "related_files": null
      }
    ],
    "total": 1,
    "keywords": "example",
    "scope": 1,
    "parent": "/"
  }
}
```

---

## 💾 存储管理 API

### 获取存储列表

#### GET /api/admin/storage/list
获取所有存储配置（需要管理员权限）

**请求头**:
```
Authorization: Bearer <token>
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "mount_path": "/local",
      "order": 1,
      "driver": "Local",
      "cache_expiration": 60,
      "status": "work",
      "addition": "{}",
      "remark": "本地存储",
      "modified": "2025-09-28T10:30:00Z",
      "disabled": false,
      "enable_sign": false,
      "order_by": "name",
      "order_direction": "asc",
      "extract_folder": "",
      "web_proxy": false,
      "webdav_policy": "native_proxy",
      "down_proxy_url": "",
      "proxy_range": false
    }
  ]
}
```

---

## 📋 任务管理 API

### 获取已完成任务

#### GET /api/admin/task/finish
获取已完成的任务列表

**请求参数**:
- `token` (header): Bearer token
- `page` (query): 页码，默认为 1
- `per_page` (query): 每页数量，默认为 20

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "id": "task_123",
        "name": "文件上传",
        "type": "upload",
        "status": "completed",
        "progress": 100,
        "created_at": "2025-09-28T10:30:00Z",
        "updated_at": "2025-09-28T10:35:00Z",
        "error": null,
        "result": {
          "file_path": "/uploads/example.pdf",
          "file_size": 1024000
        }
      }
    ],
    "total": 1,
    "page": 1,
    "per_page": 20
  }
}
```

### 获取未完成任务

#### GET /api/admin/task/unfinished
获取未完成的任务列表

**请求参数**:
- `token` (header): Bearer token
- `page` (query): 页码，默认为 1
- `per_page` (query): 每页数量，默认为 20

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "id": "task_124",
        "name": "文件下载",
        "type": "download",
        "status": "running",
        "progress": 45,
        "created_at": "2025-09-28T10:30:00Z",
        "updated_at": "2025-09-28T10:32:00Z",
        "error": null,
        "result": null
      }
    ],
    "total": 1,
    "page": 1,
    "per_page": 20
  }
}
```

---

## 📊 响应格式

### 标准响应结构

所有 API 响应都遵循统一的格式：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 状态码说明

- `200`: 成功
- `400`: 请求参数错误
- `401`: 未授权，需要登录
- `403`: 权限不足
- `404`: 资源不存在
- `500`: 服务器内部错误

### 错误处理

```json
{
  "code": 401,
  "message": "unauthorized",
  "data": null
}
```

---

## 🔒 安全注意事项

### 认证令牌
- 所有 API 调用（除登录外）都需要在请求头中包含 Bearer token
- Token 有效期由服务器配置，过期后需要重新登录
- Token 应该安全存储，建议使用 Android Keystore

### 密码保护
- 某些受保护的目录或文件需要提供密码
- 密码在传输过程中使用 HTTPS 加密
- 不要在客户端存储明文密码

### 权限控制
- 文件操作权限由 AList 服务器控制
- 管理员 API 需要管理员权限
- 用户只能访问自己有权限的文件和目录

---

## 📱 Android 客户端使用

### Retrofit 接口定义

```kotlin
interface AListApiService {
    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("/api/fs/list")
    suspend fun getFiles(
        @Header("Authorization") token: String,
        @Query("path") path: String = "/"
    ): Response<FileListResponse>

    @GET("/api/fs/link")
    suspend fun getFileLink(
        @Header("Authorization") token: String,
        @Query("path") path: String
    ): Response<FileLinkResponse>
}
```

### 网络请求示例

```kotlin
// 登录
val loginRequest = LoginRequest(username, password)
val response = apiService.login(loginRequest)

if (response.isSuccessful) {
    val token = response.body()?.data?.token
    // 保存 token
}

// 获取文件列表
val token = "Bearer $savedToken"
val response = apiService.getFiles(token, "/Documents")

if (response.isSuccessful) {
    val files = response.body()?.data?.content
    // 显示文件列表
}
```

---

## 🔄 最佳实践

### 错误处理
```kotlin
try {
    val response = apiService.getFiles(token, path)
    if (response.isSuccessful) {
        // 处理成功响应
    } else {
        // 处理 API 错误
        val errorCode = response.code()
        val errorMessage = response.message()
    }
} catch (e: Exception) {
    // 处理网络错误
}
```

### 令牌管理
```kotlin
// 自动刷新令牌
private suspend fun refreshToken(): String? {
    return try {
        val response = apiService.getCurrentUser()
        if (response.isSuccessful) {
            // 令牌仍然有效
            currentToken
        } else {
            // 重新登录
            login()
        }
    } catch (e: Exception) {
        null
    }
}
```

### 网络状态检查
```kotlin
// 检查网络连接
if (networkMonitor.isConnected.value) {
    // 执行 API 调用
} else {
    // 显示网络错误
}
```

---

*本文档详细描述了 AList Android 客户端使用的所有 API 接口，包括认证、文件管理、搜索等功能。如有任何疑问，请参考 AList 官方文档或联系技术支持。*