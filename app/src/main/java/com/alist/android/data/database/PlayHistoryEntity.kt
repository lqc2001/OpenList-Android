package com.alist.android.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "play_history")
data class PlayHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val filePath: String,
    val fileName: String,
    val fileSize: Long,
    val isVideo: Boolean,
    val duration: Long = 0,
    val currentPosition: Long = 0,
    val lastPlayedTime: Long = System.currentTimeMillis(),
    val playCount: Int = 1,
    val serverUrl: String,
    val thumbUrl: String?
)

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }
}