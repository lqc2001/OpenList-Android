package com.alist.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alist.android.data.database.PlayHistoryEntity
import com.alist.android.data.model.FileInfo
import com.alist.android.ui.screen.FileListScreen
import com.alist.android.ui.screen.VideoPlayerScreen
import com.alist.android.ui.screen.LoginScreen
import com.alist.android.ui.screen.PlayHistoryScreen
import com.alist.android.ui.theme.AListTheme
import com.alist.android.ui.viewmodel.AuthState
import com.alist.android.ui.viewmodel.SimpleAuthViewModel
import com.alist.android.ui.viewmodel.SimpleFileViewModel
import com.alist.android.ui.viewmodel.SimplePlayerViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AListTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val viewModel: SimpleAuthViewModel = viewModel(factory = SimpleAuthViewModel.Factory)
    val authState by viewModel.authState.collectAsStateWithLifecycle()

    // 播放器状态管理
    var currentPlayingFile by remember { mutableStateOf<FileInfo?>(null) }
    var showPlayer by remember { mutableStateOf(false) }
    var showPlayHistory by remember { mutableStateOf(false) }

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
        authState is AuthState.Initial || authState is AuthState.NotAuthenticated || authState is AuthState.Error -> {
            LoginScreen(
                onLoginSuccess = {
                    // 登录成功后会自动导航到主界面
                },
                viewModel = viewModel
            )
        }
        authState is AuthState.Authenticated -> {
            val fileViewModel: SimpleFileViewModel = viewModel(factory = SimpleFileViewModel.Factory)
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
                    viewModel.logout()
                },
                onShowPlayHistory = {
                    showPlayHistory = true
                },
                viewModel = fileViewModel
            )
        }
        authState is AuthState.Loading -> {
            // 加载界面
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
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