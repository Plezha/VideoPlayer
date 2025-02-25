package com.plezha.videoplayer.data.api

import com.plezha.videoplayer.data.model.VideoResponse
import retrofit2.http.GET


interface VideoApiService {
    @GET("videos.json")
    suspend fun getVideos(): List<VideoResponse>

}