package com.plezha.videoplayer.data

import com.plezha.videoplayer.data.api.VideoApiService
import com.plezha.videoplayer.data.api.YoutubeApiService
import com.plezha.videoplayer.data.dao.AppDatabase
import com.plezha.videoplayer.data.model.Video
import com.plezha.videoplayer.data.model.VideoSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DateTimePeriod.Companion.parse
import java.lang.String.format
import java.util.Locale
import javax.inject.Inject

class YoutubeRepository @Inject constructor(
    private val youtubeApi: YoutubeApiService,
    private val videoApi: VideoApiService,
    private val db: AppDatabase
) {
    private val dao = db.videoDao()

    fun getVideo(id: String) = dao.getVideoById(id)

    fun getVideos(
        count: Int = 20,
        refresh: Boolean = false
    ): Flow<List<Video>> = flow {
        if (refresh || dao.getVideoCount() == 0) {
            val youtubeVideos =
                fetchAllPlaylistVideos(playlistId = "PLNfqas4TzoJl_Mir3PB6gbCBF0R0EU0gE")
            val otherVideos = fetchAllOtherVideos()
            val allVideos = youtubeVideos.toMutableList().apply{
                addAll(otherVideos)
            }
            dao.insertAll(allVideos.shuffled().take(count))
        }
        emitAll(dao.getAll().map { it.take(count)  })
    }

    private suspend fun fetchAllOtherVideos(): List<Video> {
        return videoApi.getVideos().map {
            Video(
                id = it.id,
                title = it.title,
                duration = it.duration,
                thumbnailUrl = it.thumbnailUrl,
                videoStreamingUrl = it.videoUrl.replace("http://", "https://"),
                source = VideoSource.OTHER
            )
        }
    }

    private suspend fun fetchAllPlaylistVideos(playlistId: String): List<Video> {
        val allVideos = mutableListOf<Video>()
        var nextPageToken: String? = null
        
        do {
            val response = youtubeApi.getPlaylistVideos(
                playlistId = playlistId,
                pageToken = nextPageToken
            )
            
            response.items.map { item ->
                val videoId = item.contentDetails.videoId
                Video(
                    id = videoId,
                    title = item.snippet.title,
                    duration = parseDurationResponse(getDuration(videoId)),
                    thumbnailUrl = item.snippet.thumbnails.medium.url,
                    videoStreamingUrl = "https://www.youtube.com/embed/${videoId}",
                    source = VideoSource.YOUTUBE
                )
            }.let { allVideos.addAll(it) }
            
            nextPageToken = response.nextPageToken
        } while (nextPageToken != null)

        return allVideos
    }

    private fun parseDurationResponse(duration: String): String {
        val parsed = parse(duration)
        val locale = Locale.getDefault()
        var readableDuration = ""
        if (parsed.days != 0) readableDuration += "${parsed.days}:"
        if (parsed.hours != 0) readableDuration += "${parsed.hours}:"
        else if (readableDuration.isNotEmpty()) readableDuration += format(locale, "%02d:", parsed.hours)
        if (readableDuration.isEmpty()) readableDuration += "${parsed.minutes}:"
        else readableDuration += format(locale, "%02d:", parsed.minutes)
        readableDuration += format(locale, "%02d", parsed.seconds)

        return readableDuration
    }

    private suspend fun getDuration(videoId: String): String {
        return youtubeApi.getVideoDetails(videoId = videoId).items[0].contentDetails.duration
    }
}