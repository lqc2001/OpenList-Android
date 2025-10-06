package com.openlist.android.di

import android.app.Application
import com.openlist.android.data.api.AListApiService
import com.openlist.android.data.database.AppDatabase
import com.openlist.android.data.database.PlayHistoryDao
import com.openlist.android.data.network.NetworkModule
import com.openlist.android.data.preferences.PreferencesRepository
import com.openlist.android.data.preferences.PreferencesRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

  
    @Provides
    @Singleton
    fun providePreferencesRepository(application: Application): PreferencesRepository {
        return com.openlist.android.data.preferences.DataStoreModule.providePreferencesRepository(
            com.openlist.android.data.preferences.DataStoreModule.provideDataStore(application),
            application
        )
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        application: Application,
        preferencesRepository: PreferencesRepository
    ): OkHttpClient {
        return NetworkModule.provideOkHttpClient(
            loggingInterceptor = NetworkModule.provideHttpLoggingInterceptor(),
            application = application,
            preferencesRepository = preferencesRepository
        )
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        preferencesRepository: PreferencesRepository
    ): Retrofit {
        return NetworkModule.provideRetrofit(
            okHttpClient = okHttpClient,
            gson = NetworkModule.provideGson(),
            preferencesRepository = preferencesRepository
        )
    }

    @Provides
    @Singleton
    fun provideAListApiService(retrofit: Retrofit): AListApiService {
        return NetworkModule.provideAListApiService(retrofit)
    }

    @Provides
    @Singleton
    fun provideAppDatabase(application: Application): AppDatabase {
        return AppDatabase.getDatabase(application)
    }

    @Provides
    @Singleton
    fun providePlayHistoryDao(database: AppDatabase): PlayHistoryDao {
        return database.playHistoryDao()
    }
}