package com.openlist.android.player.core

import android.content.Context
import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.datasource.DefaultHttpDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@UnstableApi
class SimpleExoPlayerManager(
    private val context: Context
) : Player.Listener {

    private var exoPlayer: ExoPlayer? = null
    private var currentMediaItem: MediaItem? = null

    // 播放器状态
    private val _playbackState = MutableStateFlow(PlaybackState.IDLE)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    // 播放进度
    private val _currentTime = MutableStateFlow(0L)
    val currentTime: StateFlow<Long> = _currentTime.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _bufferedPercentage = MutableStateFlow(0)
    val bufferedPercentage: StateFlow<Int> = _bufferedPercentage.asStateFlow()

    // 播放控制
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    // 音轨信息（简化版本）
    private val _audioTracks = MutableStateFlow<List<String>>(emptyList())
    val audioTracks: StateFlow<List<String>> = _audioTracks.asStateFlow()

    private val _selectedAudioTrack = MutableStateFlow<String?>(null)
    val selectedAudioTrack: StateFlow<String?> = _selectedAudioTrack.asStateFlow()

    // 播放速度
    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    init {
        initializePlayer()
    }

    private fun initializePlayer() {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context).build().apply {
                addListener(this@SimpleExoPlayerManager)
                setPlayWhenReady(false)
                playbackParameters = PlaybackParameters(1.0f)
            }
        }
    }

    fun preparePlayer(url: String, headers: Map<String, String> = emptyMap()) {
        exoPlayer?.let { player ->
            try {
                val mediaItem = MediaItem.Builder()
                    .setUri(Uri.parse(url))
                    .build()

                currentMediaItem = mediaItem

                val mediaSource = buildMediaSource(mediaItem)
                player.setMediaSource(mediaSource)
                player.prepare()

                _playbackState.value = PlaybackState.PREPARING
                Log.d("SimpleExoPlayerManager", "准备播放: $url")
            } catch (e: Exception) {
                Log.e("SimpleExoPlayerManager", "准备播放失败", e)
                _playbackState.value = PlaybackState.ERROR
            }
        }
    }

    private fun buildMediaSource(mediaItem: MediaItem): MediaSource {
        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("AList-Android-Player")
            .setConnectTimeoutMs(15000)
            .setReadTimeoutMs(15000)

        return when {
            mediaItem.localConfiguration?.uri?.toString()?.endsWith(".m3u8") == true -> {
                HlsMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
            }
            else -> {
                ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
            }
        }
    }

    fun play() {
        exoPlayer?.playWhenReady = true
        _isPlaying.value = true
        Log.d("SimpleExoPlayerManager", "开始播放")
    }

    fun pause() {
        exoPlayer?.playWhenReady = false
        _isPlaying.value = false
        Log.d("SimpleExoPlayerManager", "暂停播放")
    }

    fun stop() {
        exoPlayer?.let { player ->
            player.stop()
            player.clearMediaItems()
            _playbackState.value = PlaybackState.IDLE
            _isPlaying.value = false
            _currentTime.value = 0L
            _duration.value = 0L
            Log.d("SimpleExoPlayerManager", "停止播放")
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
        Log.d("SimpleExoPlayerManager", "跳转到: ${positionMs}ms")
    }

    fun setPlaybackSpeed(speed: Float) {
        if (speed in 0.25f..4.0f) {
            exoPlayer?.setPlaybackParameters(PlaybackParameters(speed))
            _playbackSpeed.value = speed
            Log.d("SimpleExoPlayerManager", "设置播放速度: $speed")
        }
    }

    fun getAvailableAudioTracks(): List<String> {
        // 简化版本，返回空列表
        return emptyList()
    }

    fun selectAudioTrack(track: String) {
        // 简化版本，不实现音轨切换
        Log.d("SimpleExoPlayerManager", "音轨切换功能暂未实现")
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
            Player.STATE_IDLE -> {
                _playbackState.value = PlaybackState.IDLE
            }
            Player.STATE_BUFFERING -> {
                _playbackState.value = PlaybackState.BUFFERING
            }
            Player.STATE_READY -> {
                _playbackState.value = PlaybackState.READY
                _duration.value = exoPlayer?.duration ?: 0L
            }
            Player.STATE_ENDED -> {
                _playbackState.value = PlaybackState.ENDED
                _isPlaying.value = false
            }
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        _isPlaying.value = isPlaying
    }

    override fun onPositionDiscontinuity(
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int
    ) {
        _currentTime.value = newPosition.positionMs
    }

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
        _playbackSpeed.value = playbackParameters.speed
    }

    override fun onRenderedFirstFrame() {
        _playbackState.value = PlaybackState.READY
    }

    fun getCurrentPosition(): Long {
        return exoPlayer?.currentPosition ?: 0L
    }

    fun getDuration(): Long {
        return exoPlayer?.duration ?: 0L
    }

    fun isPlaying(): Boolean {
        return exoPlayer?.isPlaying == true
    }

    fun release() {
        exoPlayer?.release()
        exoPlayer = null
        _playbackState.value = PlaybackState.IDLE
        _isPlaying.value = false
        Log.d("SimpleExoPlayerManager", "释放播放器")
    }

    // 播放状态枚举
    enum class PlaybackState {
        IDLE, PREPARING, READY, BUFFERING, ENDED, ERROR
    }
}