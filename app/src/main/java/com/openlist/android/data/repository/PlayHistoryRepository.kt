package com.openlist.android.data.repository

import com.openlist.android.data.database.PlayHistoryDao
import com.openlist.android.data.database.PlayHistoryEntity
import com.openlist.android.data.model.FileInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayHistoryRepository @Inject constructor(
    private val playHistoryDao: PlayHistoryDao
) {

    suspend fun insertPlayHistory(fileInfo: FileInfo, currentPosition: Long = 0L) {
        val entity = PlayHistoryEntity(
            id = 0, // Room会自动生成ID
            filePath = fileInfo.path,
            fileName = fileInfo.name,
            fileSize = fileInfo.size,
            isVideo = !fileInfo.is_dir && (fileInfo.type == 1),
            duration = if (fileInfo.is_dir) 0L else (fileInfo.size / (1024 * 1024)), // 简单估算时长
            currentPosition = currentPosition,
            lastPlayedTime = System.currentTimeMillis(),
            playCount = 1,
            serverUrl = "", // 需要从其他地方获取
            thumbUrl = fileInfo.thumb
        )
        playHistoryDao.insertPlayHistory(entity)
    }

    suspend fun updatePlayPosition(path: String, position: Long) {
        val existing = playHistoryDao.getPlayHistoryByFilePath(path)
        existing?.let {
            val updated = it.copy(currentPosition = position)
            playHistoryDao.updatePlayHistory(updated)
        }
    }

    suspend fun deletePlayHistory(path: String) {
        playHistoryDao.deletePlayHistoryByFilePath(path)
    }

    suspend fun clearAllHistory() {
        playHistoryDao.clearAllPlayHistory()
    }

    fun getAllPlayHistory(): Flow<List<PlayHistoryEntity>> {
        return playHistoryDao.getAllPlayHistory()
    }

    fun getRecentPlayHistory(limit: Int = 50): Flow<List<PlayHistoryEntity>> {
        return playHistoryDao.getRecentPlayHistory(true) // 获取视频历史
    }

    fun getPlayHistoryByPath(path: String): Flow<PlayHistoryEntity?> {
        // 由于DAO返回的是suspend函数，我们需要转换为Flow
        TODO("需要实现此方法")
    }

    fun getVideoHistory(): Flow<List<PlayHistoryEntity>> {
        return playHistoryDao.getRecentPlayHistory(true)
    }

    fun getAudioHistory(): Flow<List<PlayHistoryEntity>> {
        return playHistoryDao.getRecentPlayHistory(false)
    }

    suspend fun isFileInHistory(path: String): Boolean {
        return playHistoryDao.getPlayHistoryByFilePath(path) != null
    }

    fun searchPlayHistory(query: String): Flow<List<PlayHistoryEntity>> {
        return playHistoryDao.searchPlayHistory(query)
    }
}