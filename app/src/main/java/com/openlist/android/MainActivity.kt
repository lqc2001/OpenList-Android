package com.openlist.android

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.AndroidEntryPoint
import com.openlist.android.data.database.PlayHistoryEntity
import com.openlist.android.data.model.FileInfo
import com.openlist.android.ui.screen.FileListScreen
import com.openlist.android.ui.screen.VideoPlayerScreen
import com.openlist.android.ui.screen.LoginScreen
import com.openlist.android.ui.screen.PlayHistoryScreen
import com.openlist.android.ui.screen.SettingsScreen
import com.openlist.android.ui.theme.AListTheme
import com.openlist.android.ui.viewmodel.AuthState
import com.openlist.android.ui.viewmodel.SimpleAuthViewModel
import com.openlist.android.ui.viewmodel.SimpleFileViewModel
import com.openlist.android.ui.viewmodel.SimplePlayerViewModel
import com.openlist.android.ui.viewmodel.AutoLoginState

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "=== MainActivity onCreate ===")
        Log.d(TAG, "Application lifecycle: onCreate")
        Log.d(TAG, "SavedInstanceState: ${if (savedInstanceState != null) "exists" else "null"}")
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Log.d(TAG, "Edge-to-edge enabled")
        setContent {
            Log.d(TAG, "Setting Compose content...")
            AListTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Log.d(TAG, "Creating AppNavigation...")
                    AppNavigation()
                }
            }
        }
        Log.d(TAG, "MainActivity onCreate completed")
    }

    override fun onBackPressed() {
        Log.d(TAG, "=== Back button pressed ===")
        val viewModel = androidx.lifecycle.ViewModelProvider(this).get(SimpleAuthViewModel::class.java)
        val currentAutoLoginState = viewModel.autoLoginState.value

        Log.d(TAG, "Current auto login state: $currentAutoLoginState")

        // 如果正在自动登录，允许用户中断
        if (currentAutoLoginState is AutoLoginState.Attempting) {
            Log.d(TAG, "Auto login in progress, cancelling due to back button press")
            viewModel.cancelAutoLogin()
            Log.d(TAG, "Auto login cancellation requested")
            // 显示中断提示
            Log.d(TAG, "Showing cancellation toast...")
            android.widget.Toast.makeText(this, "已中断自动登录", android.widget.Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Cancellation toast displayed")
        } else {
            Log.d(TAG, "No auto login in progress, calling super.onBackPressed()")
            super.onBackPressed()
        }
        Log.d(TAG, "Back button handling completed")
    }
}

@Composable
fun AppNavigation() {
    Log.d("AppNavigation", "=== AppNavigation composition started ===")
    val viewModel: SimpleAuthViewModel = viewModel()
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val autoLoginState by viewModel.autoLoginState.collectAsStateWithLifecycle()

    Log.d("AppNavigation", "Current states:")
    Log.d("AppNavigation", "  - authState: $authState")
    Log.d("AppNavigation", "  - autoLoginState: $autoLoginState")

    // 启动时尝试自动登录
    LaunchedEffect(Unit) {
        Log.d("AppNavigation", "=== App launched, starting auto login flow ===")
        Log.d("AppNavigation", "Initial authState: $authState")
        Log.d("AppNavigation", "Initial autoLoginState: $autoLoginState")
        Log.d("AppNavigation", "Calling viewModel.attemptAutoLogin()...")
        viewModel.attemptAutoLogin()
        Log.d("AppNavigation", "Auto login initiated")
    }

    // 播放器状态管理
    var currentPlayingFile by remember { mutableStateOf<FileInfo?>(null) }
    var showPlayer by remember { mutableStateOf(false) }
    var showPlayHistory by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    when {
        showPlayer && currentPlayingFile != null -> {
            VideoPlayerScreen(
                file = currentPlayingFile!!,
                onBack = {
                    showPlayer = false
                    currentPlayingFile = null
                }
            )
        }
        showPlayHistory -> {
            PlayHistoryScreen(
                onHistoryItemClick = { history ->
                    // 从历史记录继续播放
                    val historyFile = FileInfo(
                        name = history.fileName,
                        size = history.fileSize,
                        is_dir = false,
                        modified = "0",
                        sign = "",
                        thumb = history.thumbUrl,
                        type = if (history.isVideo) 1 else 2,
                        hash_info = null,
                        hash_value = null,
                        path = history.filePath,
                        parent = "",
                        proxy_url = null,
                        readme = null,
                        provider = null,
                        related_files = null
                    )
                    currentPlayingFile = historyFile
                    showPlayer = true
                    showPlayHistory = false
                },
                onBack = {
                    showPlayHistory = false
                }
            )
        }
        showSettings -> {
            SettingsScreen(
                onBack = {
                    showSettings = false
                }
            )
        }
        // 正在自动登录中，显示加载界面
        autoLoginState is AutoLoginState.Attempting -> {
            Log.d("AppNavigation", "Auto login in progress")
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "正在自动登录...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        // 自动登录失败但尚未显示登录界面
        autoLoginState is AutoLoginState.Failed -> {
            Log.d("AppNavigation", "Auto login failed: ${(autoLoginState as AutoLoginState.Failed).message}")
            LoginScreen(
                onLoginSuccess = {
                    // 登录成功后会自动导航到主界面
                },
                viewModel = viewModel
            )
        }
        // 显示登录界面
        authState is AuthState.Initial || authState is AuthState.NotAuthenticated || authState is AuthState.Error -> {
            LoginScreen(
                onLoginSuccess = {
                    // 登录成功后会自动导航到主界面
                },
                viewModel = viewModel
            )
        }
        // 已认证，显示主界面
        authState is AuthState.Authenticated -> {
            Log.d("AppNavigation", "User authenticated, showing main interface")
            val fileViewModel: SimpleFileViewModel = viewModel()
            FileListScreen(
                onFileClick = { file ->
                    handleFileClick(
                        file = file,
                        onPlay = { selectedFile ->
                            currentPlayingFile = selectedFile
                            showPlayer = true
                        }
                    )
                },
                onNavigateBack = {
                    showSettings = true
                },
                onShowPlayHistory = {
                    showPlayHistory = true
                },
                viewModel = fileViewModel
            )
        }
        // 手动登录中，显示加载界面
        authState is AuthState.Loading -> {
            Log.d("AppNavigation", "Manual login in progress")
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator()
            }
        }
    }
}

private fun handleFileClick(
    file: FileInfo,
    onPlay: (FileInfo) -> Unit
) {
    // 实现文件点击处理逻辑
    if (isMediaFile(file.name)) {
        // 打开播放器
        onPlay(file)
    } else {
        // 非媒体文件，提供其他操作（下载等）
        // TODO: 实现下载功能
    }
}

private fun isMediaFile(fileName: String): Boolean {
    val videoExtensions = listOf(".mp4", ".avi", ".mkv", ".mov", ".wmv", ".flv", ".webm", ".m4v")
    val audioExtensions = listOf(".mp3", ".wav", ".flac", ".aac", ".ogg", ".m4a", ".wma")
    val allExtensions = videoExtensions + audioExtensions
    return allExtensions.any { fileName.endsWith(it, ignoreCase = true) }
}