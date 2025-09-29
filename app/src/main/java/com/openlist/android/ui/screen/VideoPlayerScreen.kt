package com.openlist.android.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.openlist.android.data.model.FileInfo
import com.openlist.android.ui.viewmodel.SimplePlayerViewModel
import com.openlist.android.player.core.SimpleExoPlayerManager.PlaybackState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(
    file: FileInfo,
    onBack: () -> Unit,
    viewModel: SimplePlayerViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = SimplePlayerViewModel.Factory)
) {
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val currentTime by viewModel.currentTime.collectAsStateWithLifecycle()
    val duration by viewModel.duration.collectAsStateWithLifecycle()
    val currentFile by viewModel.currentFile.collectAsStateWithLifecycle()
    val audioTracks by viewModel.audioTracks.collectAsStateWithLifecycle()
    val selectedAudioTrack by viewModel.selectedAudioTrack.collectAsStateWithLifecycle()

    // 获取文件链接
    LaunchedEffect(file) {
        viewModel.playFile(file, listOf(file), "http://localhost:5244${file.path}")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentFile?.name ?: file.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (audioTracks.isNotEmpty()) {
                        var showTrackSelector by remember { mutableStateOf(false) }

                        IconButton(onClick = { showTrackSelector = true }) {
                            Icon(Icons.Default.SettingsVoice, contentDescription = "音轨选择")
                        }

                        DropdownMenu(
                            expanded = showTrackSelector,
                            onDismissRequest = { showTrackSelector = false }
                        ) {
                            audioTracks.forEach { track ->
                                DropdownMenuItem(
                                    text = { Text(track) },
                                    onClick = {
                                        viewModel.selectAudioTrack(track)
                                        showTrackSelector = false
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    PlayerView(context).apply {
                        useController = false // 禁用默认控制器，使用自定义控制器
                        resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
                    }
                },
                update = { playerView ->
                    // PlayerView 会通过 SimpleExoPlayerManager 自动绑定
                }
            )

            // 自定义播放控制器
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                // 进度条
                Slider(
                    value = if (duration > 0) currentTime.toFloat() / duration.toFloat() else 0f,
                    onValueChange = { value ->
                        if (duration > 0) {
                            viewModel.seekTo((value * duration).toLong())
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // 时间显示和控制按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${viewModel.formatDuration(currentTime)} / ${viewModel.formatDuration(duration)}",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = { viewModel.playPrevious() }) {
                            Icon(Icons.Default.SkipPrevious, contentDescription = "上一个")
                        }

                        IconButton(onClick = { viewModel.togglePlayPause() }) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "暂停" else "播放"
                            )
                        }

                        IconButton(onClick = { viewModel.playNext() }) {
                            Icon(Icons.Default.SkipNext, contentDescription = "下一个")
                        }
                    }
                }
            }

            // 加载状态
            if (playbackState == PlaybackState.BUFFERING) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}