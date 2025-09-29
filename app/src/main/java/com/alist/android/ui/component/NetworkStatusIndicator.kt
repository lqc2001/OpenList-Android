package com.alist.android.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alist.android.data.network.NetworkConnectivityMonitor

@Composable
fun NetworkStatusIndicator(
    networkMonitor: NetworkConnectivityMonitor,
    modifier: Modifier = Modifier
) {
    val isConnected by networkMonitor.isConnected.collectAsStateWithLifecycle()
    val connectionType by networkMonitor.connectionType.collectAsStateWithLifecycle()
    val connectionQuality by networkMonitor.connectionQuality.collectAsStateWithLifecycle()

    if (!isConnected) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFEBEE)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.WifiOff,
                    contentDescription = "网络断开",
                    tint = Color.Red
                )
                Text(
                    text = "网络连接已断开",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    } else {
        // 显示网络质量指示器
        val (icon, tint, text) = when (connectionQuality) {
            NetworkConnectivityMonitor.ConnectionQuality.EXCELLENT ->
                Triple(Icons.Default.Wifi, Color.Green, "网络优秀")
            NetworkConnectivityMonitor.ConnectionQuality.GOOD ->
                Triple(Icons.Default.Wifi, Color(0xFF4CAF50), "网络良好")
            NetworkConnectivityMonitor.ConnectionQuality.FAIR ->
                Triple(Icons.Default.Wifi, Color(0xFFFF9800), "网络一般")
            NetworkConnectivityMonitor.ConnectionQuality.POOR ->
                Triple(Icons.Default.Wifi, Color.Red, "网络较差")
        }

        val typeText = when (connectionType) {
            NetworkConnectivityMonitor.ConnectionType.WIFI -> "WiFi"
            NetworkConnectivityMonitor.ConnectionType.CELLULAR -> "移动网络"
            NetworkConnectivityMonitor.ConnectionType.ETHERNET -> "有线网络"
            else -> "未知网络"
        }

        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF5F5F5)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = tint,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "$typeText · $text",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun NetworkStatusBanner(
    networkMonitor: NetworkConnectivityMonitor,
    modifier: Modifier = Modifier
) {
    val isConnected by networkMonitor.isConnected.collectAsStateWithLifecycle()
    val connectionQuality by networkMonitor.connectionQuality.collectAsStateWithLifecycle()

    if (!isConnected) {
        Banner(
            modifier = modifier,
            containerColor = Color(0xFFFFEBEE),
            contentColor = Color.Red
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "网络错误",
                    tint = Color.Red
                )
                Text(
                    text = "网络连接已断开，请检查网络设置",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    } else if (connectionQuality == NetworkConnectivityMonitor.ConnectionQuality.POOR) {
        Banner(
            modifier = modifier,
            containerColor = Color(0xFFFFF8E1),
            contentColor = Color(0xFF3E2723)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SignalCellularAlt1Bar,
                    contentDescription = "网络信号弱",
                    tint = Color(0xFF3E2723)
                )
                Text(
                    text = "网络信号较弱，可能影响使用体验",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun Banner(
    modifier: Modifier = Modifier,
    containerColor: Color,
    contentColor: Color,
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = containerColor,
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}