package com.openlist.android.di

import android.app.Application
import com.openlist.android.data.api.AListApiService
import com.openlist.android.data.database.PlayHistoryDao
import com.openlist.android.data.preferences.PreferencesRepository
import com.openlist.android.data.repository.FileRepository
import com.openlist.android.data.repository.AuthRepository
import com.openlist.android.data.repository.PlayHistoryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        application: Application,
        preferencesRepository: PreferencesRepository
    ): AuthRepository {
        return AuthRepository(application, preferencesRepository)
    }

    @Provides
    @Singleton
    fun provideFileRepository(
        apiService: AListApiService,
        preferencesRepository: PreferencesRepository
    ): FileRepository {
        return FileRepository(apiService, preferencesRepository)
    }

    @Provides
    @Singleton
    fun providePlayHistoryRepository(
        playHistoryDao: PlayHistoryDao
    ): PlayHistoryRepository {
        return PlayHistoryRepository(playHistoryDao)
    }
}