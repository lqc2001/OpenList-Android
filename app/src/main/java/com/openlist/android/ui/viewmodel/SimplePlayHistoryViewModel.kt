package com.openlist.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openlist.android.data.database.PlayHistoryEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SimplePlayHistoryViewModel : ViewModel() {

    private val _playHistory = MutableStateFlow<List<PlayHistoryEntity>>(emptyList())
    val playHistory: StateFlow<List<PlayHistoryEntity>> = _playHistory.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadPlayHistory() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 简化版本，暂时使用空列表
                _playHistory.value = emptyList()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchHistory(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 简化版本，暂时使用空列表
                _playHistory.value = emptyList()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteHistory(history: PlayHistoryEntity) {
        viewModelScope.launch {
            try {
                // 简化版本，暂时不实现删除
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            try {
                _playHistory.value = emptyList()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun formatDuration(duration: Long): String {
        val hours = duration / 3600000
        val minutes = (duration % 3600000) / 60000
        val seconds = (duration % 60000) / 1000

        return when {
            hours > 0 -> String.format("%02d:%02d:%02d", hours, minutes, seconds)
            else -> String.format("%02d:%02d", minutes, seconds)
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

    fun getProgressPercentage(currentPosition: Long, duration: Long): Int {
        return if (duration > 0) {
            ((currentPosition.toFloat() / duration.toFloat()) * 100).toInt()
        } else {
            0
        }
    }
}