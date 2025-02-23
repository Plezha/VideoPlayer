package com.plezha.videoplayer.data.api

import com.plezha.videoplayer.data.model.VideoDetailsResponse
import com.plezha.videoplayer.data.model.YoutubePlaylistResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface YoutubeApiService {
    @GET("playlistItems")
    suspend fun getPlaylistVideos(
        @Query("part") part: String = "snippet,contentDetails",
        @Query("playlistId") playlistId: String,
        @Query("maxResults") maxResults: Int = 50,
        @Query("pageToken") pageToken: String? = null,
        @Query("key") key: String = YoutubeApiConfig.API_KEY
    ): YoutubePlaylistResponse

    @GET("videos")
    suspend fun getVideoDetails(
        @Query("part") part: String = "contentDetails",
        @Query("id") videoId: String,
        @Query("key") key: String = YoutubeApiConfig.API_KEY
    ): VideoDetailsResponse
}

