package com.openlist.android.data.error

open class OpenListException(message: String, cause: Throwable? = null) : Exception(message, cause)

class NetworkException(
    message: String,
    val errorCode: Int? = null,
    cause: Throwable? = null
) : OpenListException(message, cause)

class AuthenticationException(
    message: String = "认证失败",
    cause: Throwable? = null
) : OpenListException(message, cause)

class ServerException(
    message: String,
    val statusCode: Int,
    cause: Throwable? = null
) : OpenListException(message, cause)

class FileSystemException(
    message: String,
    val filePath: String? = null,
    cause: Throwable? = null
) : OpenListException(message, cause)

class MediaPlaybackException(
    message: String,
    val mediaPath: String? = null,
    cause: Throwable? = null
) : OpenListException(message, cause)

class DataStorageException(
    message: String,
    cause: Throwable? = null
) : OpenListException(message, cause)

class ValidationException(
    message: String,
    val fieldName: String? = null,
    cause: Throwable? = null
) : OpenListException(message, cause)