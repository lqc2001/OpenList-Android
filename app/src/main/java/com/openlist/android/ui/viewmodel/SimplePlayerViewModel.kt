package com.openlist.android.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openlist.android.data.model.FileInfo
import com.openlist.android.player.core.SimpleExoPlayerManager
import com.openlist.android.player.core.SimpleExoPlayerManager.PlaybackState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras

class SimplePlayerViewModel(
    private val application: android.app.Application
) : ViewModel() {

    // 播放器管理器
    private val exoPlayerManager by lazy {
        SimpleExoPlayerManager(application)
    }

    // 当前播放文件
    private val _currentFile = MutableStateFlow<FileInfo?>(null)
    val currentFile: StateFlow<FileInfo?> = _currentFile.asStateFlow()

    // 播放列表
    private val _playlist = MutableStateFlow<List<FileInfo>>(emptyList())
    val playlist: StateFlow<List<FileInfo>> = _playlist.asStateFlow()

    private val _currentPlaylistIndex = MutableStateFlow(0)
    val currentPlaylistIndex: StateFlow<Int> = _currentPlaylistIndex.asStateFlow()

    // 播放器状态
    private val _playbackState = MutableStateFlow(PlaybackState.IDLE)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _currentTime = MutableStateFlow(0L)
    val currentTime: StateFlow<Long> = _currentTime.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _bufferedPercentage = MutableStateFlow(0)
    val bufferedPercentage: StateFlow<Int> = _bufferedPercentage.asStateFlow()

    // 音轨控制
    private val _audioTracks = MutableStateFlow<List<String>>(emptyList())
    val audioTracks: StateFlow<List<String>> = _audioTracks.asStateFlow()

    private val _selectedAudioTrack = MutableStateFlow<String?>(null)
    val selectedAudioTrack: StateFlow<String?> = _selectedAudioTrack.asStateFlow()

    // 播放速度
    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    // 播放进度更新
    private var progressUpdateJob: kotlinx.coroutines.Job? = null

    init {
        startProgressUpdate()
    }

    private fun startProgressUpdate() {
        progressUpdateJob = viewModelScope.launch {
            while (isActive) {
                if (exoPlayerManager.isPlaying()) {
                    _currentTime.value = exoPlayerManager.getCurrentPosition()
                    _duration.value = exoPlayerManager.getDuration()
                    _bufferedPercentage.value = calculateBufferedPercentage()
                }
                delay(1000) // 每秒更新一次
            }
        }
    }

    private fun calculateBufferedPercentage(): Int {
        val duration = exoPlayerManager.getDuration()
        val currentPosition = exoPlayerManager.getCurrentPosition()

        if (duration <= 0) return 0

        // 简单的缓冲百分比计算
        val bufferedDuration = exoPlayerManager.getDuration() - currentPosition
        return ((bufferedDuration.toFloat() / duration.toFloat()) * 100).toInt()
    }

    fun playFile(file: FileInfo, playlist: List<FileInfo> = listOf(file), playUrl: String) {
        viewModelScope.launch {
            _currentFile.value = file
            _playlist.value = playlist
            _currentPlaylistIndex.value = playlist.indexOf(file)

            // 准备播放器
            exoPlayerManager.preparePlayer(playUrl)
            exoPlayerManager.play()

            // 更新播放状态
            updatePlayerStates()
        }
    }

    fun play() {
        exoPlayerManager.play()
        _isPlaying.value = true
    }

    fun pause() {
        exoPlayerManager.pause()
        _isPlaying.value = false
    }

    fun togglePlayPause() {
        if (exoPlayerManager.isPlaying()) {
            pause()
        } else {
            play()
        }
    }

    fun stop() {
        exoPlayerManager.stop()
        _isPlaying.value = false
        _currentTime.value = 0L
        _playbackState.value = PlaybackState.IDLE
    }

    fun seekTo(positionMs: Long) {
        exoPlayerManager.seekTo(positionMs)
        _currentTime.value = positionMs
    }

    fun setPlaybackSpeed(speed: Float) {
        exoPlayerManager.setPlaybackSpeed(speed)
        _playbackSpeed.value = speed
    }

    fun selectAudioTrack(track: String) {
        exoPlayerManager.selectAudioTrack(track)
        _selectedAudioTrack.value = track
    }

    fun playPrevious() {
        val currentIndex = _currentPlaylistIndex.value
        if (currentIndex > 0) {
            val previousFile = _playlist.value[currentIndex - 1]
            // 简化处理，直接播放
            _currentFile.value = previousFile
            _currentPlaylistIndex.value = currentIndex - 1
        }
    }

    fun playNext() {
        val currentIndex = _currentPlaylistIndex.value
        if (currentIndex < _playlist.value.size - 1) {
            val nextFile = _playlist.value[currentIndex + 1]
            // 简化处理，直接播放
            _currentFile.value = nextFile
            _currentPlaylistIndex.value = currentIndex + 1
        }
    }

    private fun updatePlayerStates() {
        _playbackState.value = PlaybackState.READY
        _isPlaying.value = exoPlayerManager.isPlaying()
        _duration.value = exoPlayerManager.getDuration()
        _audioTracks.value = exoPlayerManager.audioTracks.value
        _selectedAudioTrack.value = exoPlayerManager.selectedAudioTrack.value
    }

    private fun isVideoFile(fileName: String): Boolean {
        val videoExtensions = listOf(".mp4", ".avi", ".mkv", ".mov", ".wmv", ".flv", ".webm", ".m4v")
        return videoExtensions.any { fileName.endsWith(it, ignoreCase = true) }
    }

    fun isAudioFile(fileName: String): Boolean {
        val audioExtensions = listOf(".mp3", ".wav", ".flac", ".aac", ".ogg", ".m4a", ".wma")
        return audioExtensions.any { fileName.endsWith(it, ignoreCase = true) }
    }

    fun formatDuration(durationMs: Long): String {
        val hours = durationMs / (1000 * 60 * 60)
        val minutes = (durationMs % (1000 * 60 * 60)) / (1000 * 60)
        val seconds = (durationMs % (1000 * 60)) / 1000

        return when {
            hours > 0 -> String.format("%02d:%02d:%02d", hours, minutes, seconds)
            else -> String.format("%02d:%02d", minutes, seconds)
        }
    }

    override fun onCleared() {
        super.onCleared()
        progressUpdateJob?.cancel()
        exoPlayerManager.release()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as android.app.Application
                return SimplePlayerViewModel(application) as T
            }
        }
    }
}