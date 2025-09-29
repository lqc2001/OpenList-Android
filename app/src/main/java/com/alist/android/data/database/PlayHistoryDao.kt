package com.alist.android.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayHistoryDao {

    @Query("SELECT * FROM play_history ORDER BY lastPlayedTime DESC")
    fun getAllPlayHistory(): Flow<List<PlayHistoryEntity>>

    @Query("SELECT * FROM play_history WHERE filePath = :filePath LIMIT 1")
    suspend fun getPlayHistoryByFilePath(filePath: String): PlayHistoryEntity?

    @Query("SELECT * FROM play_history WHERE isVideo = :isVideo ORDER BY lastPlayedTime DESC LIMIT 20")
    fun getRecentPlayHistory(isVideo: Boolean): Flow<List<PlayHistoryEntity>>

    @Query("SELECT * FROM play_history WHERE fileName LIKE '%' || :query || '%' ORDER BY lastPlayedTime DESC")
    fun searchPlayHistory(query: String): Flow<List<PlayHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayHistory(history: PlayHistoryEntity)

    @Update
    suspend fun updatePlayHistory(history: PlayHistoryEntity)

    @Delete
    suspend fun deletePlayHistory(history: PlayHistoryEntity)

    @Query("DELETE FROM play_history WHERE filePath = :filePath")
    suspend fun deletePlayHistoryByFilePath(filePath: String)

    @Query("DELETE FROM play_history")
    suspend fun clearAllPlayHistory()

    @Query("DELETE FROM play_history WHERE lastPlayedTime < :timestamp")
    suspend fun clearOldPlayHistory(timestamp: Long)

    @Query("UPDATE play_history SET playCount = playCount + 1, lastPlayedTime = :currentTime WHERE filePath = :filePath")
    suspend fun incrementPlayCount(filePath: String, currentTime: Long)
}