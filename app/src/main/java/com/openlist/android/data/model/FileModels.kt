package com.openlist.android.data.model

data class FileListResponse(
    val code: Int,
    val message: String,
    val data: FileListData
)

data class FileListData(
    val content: List<FileInfo>,
    val total: Int,
    val path: String,
    val readme: String?,
    val write: Boolean,
    val provider: String
)

data class FileInfo(
    val name: String,
    val size: Long,
    val is_dir: Boolean,
    val modified: String,
    val sign: String,
    val thumb: String?,
    val type: Int,
    val hash_info: String?,
    val hash_value: String?,
    val path: String,
    val parent: String,
    val proxy_url: String?,
    val readme: String?,
    val provider: String?,
    val related_files: List<RelatedFile>?
)

data class RelatedFile(
    val name: String,
    val size: Long,
    val is_dir: Boolean,
    val modified: String,
    val sign: String,
    val thumb: String?,
    val type: Int,
    val hash_info: String?,
    val hash_value: String?,
    val path: String,
    val parent: String,
    val proxy_url: String?,
    val readme: String?,
    val provider: String?
)

data class FileLinkResponse(
    val code: Int,
    val message: String,
    val data: FileLinkData
)

data class FileLinkData(
    val raw_url: String,
    val url: String,
    val task_url: String?
)

data class UploadResponse(
    val code: Int,
    val message: String,
    val data: UploadData
)

data class UploadData(
    val upload_url: String,
    val callback_url: String
)

data class SearchResponse(
    val code: Int,
    val message: String,
    val data: SearchData
)

data class SearchData(
    val content: List<FileInfo>,
    val total: Int,
    val keywords: String,
    val scope: Int,
    val parent: String
)