package com.openlist.android.data.model

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val user: User
)

data class User(
    val id: Int,
    val username: String,
    val password: String,
    val base_path: String,
    val role: Int,
    val disabled: Boolean,
    val permission: Int,
    val sso_id: String?,
    val sso_official: Boolean?
)

data class SavedCredentials(
    val serverUrl: String,
    val username: String,
    val password: String,
    val authToken: String,
    val rememberMe: Boolean,
    val refreshToken: String
)