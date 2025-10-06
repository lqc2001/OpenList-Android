package com.openlist.android.data.database

import android.content.Context
import androidx.room.Room

object DatabaseModule {

    fun provideAppDatabase(
        context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "openlist_database"
        ).fallbackToDestructiveMigration()
         .build()
    }

    fun providePlayHistoryDao(
        database: AppDatabase
    ): PlayHistoryDao {
        return database.playHistoryDao()
    }
}