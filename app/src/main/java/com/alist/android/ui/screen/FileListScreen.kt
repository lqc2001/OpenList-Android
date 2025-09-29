package com.alist.android.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alist.android.ui.theme.AListTheme
import com.alist.android.data.model.FileInfo
import com.alist.android.ui.viewmodel.SimpleFileViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileListScreen(
    viewModel: SimpleFileViewModel = viewModel(),
    onFileClick: (FileInfo) -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onShowPlayHistory: () -> Unit = {}
) {
    val files by viewModel.files.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val currentPath by viewModel.currentPath.collectAsStateWithLifecycle()
    val selectedFiles by viewModel.selectedFiles.collectAsStateWithLifecycle()

    LaunchedEffect(error) {
        if (error != null) {
            // 显示错误信息
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = currentPath.ifEmpty { "/" },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    if (currentPath != "/") {
                        IconButton(onClick = { viewModel.navigateToParent() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                        }
                    } else {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.Menu, contentDescription = "菜单")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                    }
                    IconButton(onClick = { /* 搜索功能 */ }) {
                        Icon(Icons.Default.Search, contentDescription = "搜索")
                    }
                    IconButton(onClick = onShowPlayHistory) {
                        Icon(Icons.Default.History, contentDescription = "播放历史")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* 新建文件夹 */ }
            ) {
                Icon(Icons.Default.CreateNewFolder, contentDescription = "新建文件夹")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = error ?: "未知错误",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.refresh() }) {
                            Text("重试")
                        }
                    }
                }
                else -> {
                    if (files.isEmpty()) {
                        Text(
                            text = "此文件夹为空",
                            modifier = Modifier.align(Alignment.Center),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            items(files) { file ->
                                FileItem(
                                    file = file,
                                    isSelected = selectedFiles.contains(file),
                                    onClick = {
                                        if (file.is_dir) {
                                            viewModel.navigateToFolder(file)
                                        } else {
                                            onFileClick(file)
                                        }
                                    },
                                    onLongClick = { viewModel.selectFile(file) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FileItem(
    file: FileInfo,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 文件图标
            Icon(
                imageVector = getFileIcon(file),
                contentDescription = if (file.is_dir) "文件夹" else "文件",
                modifier = Modifier.size(40.dp),
                tint = if (file.is_dir)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.width(16.dp))

            // 文件信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = formatFileSize(file.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = formatDate(file.modified),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 更多选项
            IconButton(
                onClick = { /* 显示更多选项 */ }
            ) {
                Icon(Icons.Default.MoreVert, contentDescription = "更多选项")
            }
        }
    }
}

@Composable
fun getFileIcon(file: FileInfo): ImageVector {
    return when {
        file.is_dir -> Icons.Default.Folder
        file.name.endsWith(".mp4", ignoreCase = true) ||
        file.name.endsWith(".avi", ignoreCase = true) ||
        file.name.endsWith(".mkv", ignoreCase = true) -> Icons.Default.VideoFile
        file.name.endsWith(".mp3", ignoreCase = true) ||
        file.name.endsWith(".wav", ignoreCase = true) ||
        file.name.endsWith(".flac", ignoreCase = true) -> Icons.Default.AudioFile
        file.name.endsWith(".pdf", ignoreCase = true) -> Icons.Default.PictureAsPdf
        file.name.endsWith(".jpg", ignoreCase = true) ||
        file.name.endsWith(".png", ignoreCase = true) ||
        file.name.endsWith(".gif", ignoreCase = true) -> Icons.Default.Image
        file.name.endsWith(".txt", ignoreCase = true) -> Icons.Default.Description
        file.name.endsWith(".zip", ignoreCase = true) ||
        file.name.endsWith(".rar", ignoreCase = true) -> Icons.Default.Archive
        else -> Icons.Default.InsertDriveFile
    }
}

fun formatFileSize(size: Long): String {
    return when {
        size < 1024 -> "$size B"
        size < 1024 * 1024 -> "${size / 1024} KB"
        size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} MB"
        else -> "${size / (1024 * 1024 * 1024)} GB"
    }
}

fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}