package com.openlist.android.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.openlist.android.ui.viewmodel.AuthState
import com.openlist.android.ui.viewmodel.SimpleAuthViewModel
import com.openlist.android.data.network.NetworkConnectivityMonitor
import com.openlist.android.data.utils.UrlUtils
import com.openlist.android.ui.component.NetworkStatusBanner
import com.openlist.android.ui.component.NetworkDiagnosticsDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: SimpleAuthViewModel = viewModel()
) {
    var serverUrl by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var urlError by remember { mutableStateOf<String?>(null) }
    var showUrlSuggestion by remember { mutableStateOf(false) }
    var showNetworkDiagnostics by remember { mutableStateOf(false) }

    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val savedCredentials by viewModel.savedCredentials.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    // 网络连接监控器
    val networkMonitor = remember { NetworkConnectivityMonitor.getInstance(viewModel.application) }

    LaunchedEffect(savedCredentials) {
        savedCredentials?.let { credentials ->
            serverUrl = credentials.serverUrl ?: ""
            username = credentials.username ?: ""
            password = credentials.password ?: ""
            rememberMe = credentials.rememberMe
        }
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                onLoginSuccess()
            }
            is AuthState.Error -> {
                // 显示错误信息
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            NetworkStatusBanner(networkMonitor = networkMonitor)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        // Logo and Title
        Icon(
            imageVector = Icons.Filled.Cloud,
            contentDescription = "AList Logo",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "AList 管理器",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Android 文件管理工具",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Server URL Input
        OutlinedTextField(
            value = serverUrl,
            onValueChange = {
                serverUrl = it
                // 实时验证URL格式
                urlError = if (it.isNotBlank() && !UrlUtils.isValidUrl(it)) {
                    "服务器地址格式不正确，请包含完整的协议（如 https://j.yzfy）"
                } else {
                    null
                }

                // 检查是否需要显示格式化建议
                showUrlSuggestion = it.isNotBlank() &&
                    !UrlUtils.isValidUrl(it) &&
                    !it.startsWith("http://") &&
                    !it.startsWith("https://")
            },
            label = { Text("服务器地址") },
            placeholder = { Text("j.yzfy 或 192.168.1.100:5244") },
            leadingIcon = {
                Icon(Icons.Filled.Link, contentDescription = "服务器地址")
            },
            supportingText = {
                urlError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            isError = urlError != null,
            modifier = Modifier
                .fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
        )

        // URL格式化建议
        if (showUrlSuggestion && serverUrl.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "建议格式",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "https://${serverUrl}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        TextButton(
                            onClick = {
                                serverUrl = "https://${serverUrl}"
                                showUrlSuggestion = false
                                urlError = null
                            }
                        ) {
                            Text("使用HTTPS")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = {
                                serverUrl = "http://${serverUrl}"
                                showUrlSuggestion = false
                                urlError = null
                            }
                        ) {
                            Text("使用HTTP")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Username Input
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("用户名") },
            leadingIcon = {
                Icon(Icons.Filled.Person, contentDescription = "用户名")
            },
            modifier = Modifier
                .fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password Input
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("密码") },
            leadingIcon = {
                Icon(Icons.Filled.Lock, contentDescription = "密码")
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (passwordVisible) "隐藏密码" else "显示密码"
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Remember Me Checkbox
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = rememberMe,
                onCheckedChange = { rememberMe = it }
            )
            Text(
                text = "记住我",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Login Button
        Button(
            onClick = {
                // 先验证URL格式
                if (serverUrl.isNotBlank() && !UrlUtils.isValidUrl(serverUrl)) {
                    urlError = "服务器地址格式不正确，请包含完整的协议（如 https://j.yzfy）"
                    return@Button
                }

                // 保存服务器URL并登录
                val saved = viewModel.saveServerUrl(serverUrl)
                if (saved) {
                    urlError = null
                    showUrlSuggestion = false
                    viewModel.login(username, password, rememberMe)
                } else {
                    urlError = "服务器地址格式无效"
                }
            },
            modifier = Modifier
                .fillMaxWidth(),
            enabled = serverUrl.isNotBlank() && username.isNotBlank() && password.isNotBlank() && urlError == null && authState !is AuthState.Loading
        ) {
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("登录", modifier = Modifier.padding(8.dp))
            }
        }

        // Error Message with Network Diagnostics
        if (authState is AuthState.Error) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Error,
                            contentDescription = "错误",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = (authState as AuthState.Error).message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if ((authState as AuthState.Error).message.contains("网络") ||
                        (authState as AuthState.Error).message.contains("连接") ||
                        (authState as AuthState.Error).message.contains("超时")) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { showNetworkDiagnostics = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.NetworkPing, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("网络诊断")
                        }
                    }
                }
            }
        }

        // Network Diagnostics Button
        if (serverUrl.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = { showNetworkDiagnostics = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.NetworkPing, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("测试服务器连接")
            }
        }
    }
    }

    // Network Diagnostics Dialog
    if (showNetworkDiagnostics && serverUrl.isNotBlank()) {
        NetworkDiagnosticsDialog(
            serverUrl = serverUrl,
            networkMonitor = networkMonitor,
            onDismiss = { showNetworkDiagnostics = false },
            onRetry = {
                scope.launch {
                    val saved = viewModel.saveServerUrl(serverUrl)
                    if (saved) {
                        urlError = null
                        viewModel.login(username, password, rememberMe)
                    }
                }
            }
        )
    }
}