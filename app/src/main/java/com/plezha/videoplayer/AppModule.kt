package com.plezha.videoplayer

import android.content.Context
import androidx.room.Room
import com.plezha.videoplayer.data.YoutubeRepository
import com.plezha.videoplayer.data.api.VideoApiConfig
import com.plezha.videoplayer.data.api.VideoApiService
import com.plezha.videoplayer.data.api.YoutubeApiConfig
import com.plezha.videoplayer.data.api.YoutubeApiService
import com.plezha.videoplayer.data.dao.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    fun provideDatabase(@ApplicationContext context: Context) =
        Room.databaseBuilder(context, AppDatabase::class.java, "app.db")
            .build()

    @Provides
    fun provideYoutubeApi(): YoutubeApiService = Retrofit.Builder()
        .baseUrl(YoutubeApiConfig.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(YoutubeApiService::class.java)

    @Provides
    fun provideVideoApi(): VideoApiService = Retrofit.Builder()
        .baseUrl(VideoApiConfig.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(VideoApiService::class.java)

    @Provides
    fun provideRepository(
        youtubeApi: YoutubeApiService,
        videoApi: VideoApiService,
        db: AppDatabase
    ) = YoutubeRepository(youtubeApi, videoApi, db)
}