package com.alist.android.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.alist.android.data.network.NetworkConnectivityMonitor
import com.alist.android.data.utils.NetworkDiagnostics
import com.alist.android.data.utils.NetworkDiagnosticsInfo
import com.alist.android.data.utils.NetworkTestResult
import kotlinx.coroutines.launch

@Composable
fun NetworkDiagnosticsDialog(
    serverUrl: String,
    networkMonitor: NetworkConnectivityMonitor,
    onDismiss: () -> Unit,
    onRetry: () -> Unit
) {
    var diagnostics by remember { mutableStateOf<NetworkDiagnosticsInfo?>(null) }
    var isTesting by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(serverUrl) {
        if (serverUrl.isNotEmpty()) {
            isTesting = true
            scope.launch {
                diagnostics = NetworkDiagnostics.getNetworkDiagnostics(
                    networkMonitor.contextForDiagnostics,
                    serverUrl
                )
                isTesting = false
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // 标题
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "网络诊断",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "关闭")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isTesting) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    diagnostics?.let { diag ->
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // 网络连接状态
                            item {
                                DiagnosisItem(
                                    title = "网络连接",
                                    status = diag.isConnected,
                                    icon = Icons.Filled.Wifi,
                                    details = when (diag.connectionType) {
                                        NetworkConnectivityMonitor.ConnectionType.WIFI -> "WiFi连接"
                                        NetworkConnectivityMonitor.ConnectionType.CELLULAR -> "移动网络"
                                        NetworkConnectivityMonitor.ConnectionType.ETHERNET -> "有线网络"
                                        else -> "未知网络"
                                    }
                                )
                            }

                            // 网络质量
                            item {
                                DiagnosisItem(
                                    title = "网络质量",
                                    status = diag.connectionQuality != NetworkConnectivityMonitor.ConnectionQuality.POOR,
                                    icon = Icons.Filled.SignalCellularAlt,
                                    details = when (diag.connectionQuality) {
                                        NetworkConnectivityMonitor.ConnectionQuality.EXCELLENT -> "优秀"
                                        NetworkConnectivityMonitor.ConnectionQuality.GOOD -> "良好"
                                        NetworkConnectivityMonitor.ConnectionQuality.FAIR -> "一般"
                                        else -> "较差"
                                    }
                                )
                            }

                            // 互联网访问
                            item {
                                DiagnosisItem(
                                    title = "互联网访问",
                                    status = diag.hasInternet && diag.isValidated,
                                    icon = Icons.Filled.Language,
                                    details = if (diag.hasInternet && diag.isValidated) "正常" else "异常"
                                )
                            }

                            // 服务器连接
                            item {
                                ServerConnectionItem(diag.testResult)
                            }

                            // 优化建议
                            val suggestions = NetworkDiagnostics.getOptimizationSuggestions(diag)
                            if (suggestions.isNotEmpty()) {
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp)
                                        ) {
                                            Text(
                                                text = "优化建议",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            suggestions.forEach { suggestion ->
                                                Text(
                                                    text = "• $suggestion",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 操作按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("关闭")
                    }

                    Button(
                        onClick = {
                            onDismiss()
                            onRetry()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("重试连接")
                    }
                }
            }
        }
    }
}

@Composable
fun DiagnosisItem(
    title: String,
    status: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    details: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (status) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (status) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (status) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer
                    }
                )
                Text(
                    text = details,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (status) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer
                    }
                )
            }
            Icon(
                imageVector = if (status) Icons.Filled.CheckCircle else Icons.Filled.Error,
                contentDescription = null,
                tint = if (status) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
        }
    }
}

@Composable
fun ServerConnectionItem(testResult: NetworkTestResult) {
    val (status, message, color) = when (testResult) {
        is NetworkTestResult.Success -> Triple(true, "连接正常", MaterialTheme.colorScheme.primary)
        is NetworkTestResult.AuthRequired -> Triple(true, "需要认证", MaterialTheme.colorScheme.secondary)
        is NetworkTestResult.Timeout -> Triple(false, "连接超时", MaterialTheme.colorScheme.error)
        is NetworkTestResult.ConnectionFailed -> Triple(false, "连接失败", MaterialTheme.colorScheme.error)
        is NetworkTestResult.HostNotFound -> Triple(false, "服务器未找到", MaterialTheme.colorScheme.error)
        is NetworkTestResult.ServerNotFound -> Triple(false, "服务未启动", MaterialTheme.colorScheme.error)
        is NetworkTestResult.ServerError -> Triple(false, "服务器错误", MaterialTheme.colorScheme.error)
        is NetworkTestResult.UnknownError -> Triple(false, "未知错误 (${testResult.code})", MaterialTheme.colorScheme.error)
        is NetworkTestResult.NetworkError -> Triple(false, "网络错误: ${testResult.message}", MaterialTheme.colorScheme.error)
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (status) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Dns,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "服务器连接",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (status) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer
                    }
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (status) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer
                    }
                )
            }
            Icon(
                imageVector = if (status) Icons.Filled.CheckCircle else Icons.Filled.Error,
                contentDescription = null,
                tint = color
            )
        }
    }
}