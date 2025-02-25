package com.plezha.videoplayer

import com.plezha.videoplayer.data.VideoRepository
import com.plezha.videoplayer.data.api.VideoApiService
import com.plezha.videoplayer.data.api.YoutubeApiService
import com.plezha.videoplayer.data.dao.AppDatabase
import com.plezha.videoplayer.data.dao.VideoDao
import com.plezha.videoplayer.data.model.ContentDetails
import com.plezha.videoplayer.data.model.PlaylistItem
import com.plezha.videoplayer.data.model.Snippet
import com.plezha.videoplayer.data.model.Thumbnail
import com.plezha.videoplayer.data.model.Thumbnails
import com.plezha.videoplayer.data.model.Video
import com.plezha.videoplayer.data.model.VideoContentDetails
import com.plezha.videoplayer.data.model.VideoDetailsItem
import com.plezha.videoplayer.data.model.VideoDetailsResponse
import com.plezha.videoplayer.data.model.VideoResponse
import com.plezha.videoplayer.data.model.VideoSource
import com.plezha.videoplayer.data.model.YoutubePlaylistResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class VideoRepositoryTest {
    private val youtubeApi = mockk<YoutubeApiService>()
    private val videoApi = mockk<VideoApiService>()
    private val db = mockk<AppDatabase>()
    private val dao = mockk<VideoDao>()

    private lateinit var repository: VideoRepository

    @Before
    fun setup() {
        every { db.videoDao() } returns dao
        repository = VideoRepository(youtubeApi, videoApi, db)
        coEvery {
            youtubeApi.getVideoDetails(
                part = any(),
                videoId = any(),
                key = any()
            )
        } returns videoDetailsResponse("PT5M30S")
    }

    @Test
    fun `getVideos should fetch from API when cache is empty`() = runTest {
        val youtubeVideos = listOf(videoMock("1", VideoSource.YOUTUBE))
        val otherVideos = listOf(videoMock("2", VideoSource.OTHER))

        coEvery { youtubeApi.getPlaylistVideos(any(), any()) } returns playlistResponse()
        coEvery { videoApi.getVideos() } returns otherVideos.map { it.toApiModel() }
        coEvery { dao.getVideoCount() } returns 0
        coEvery { dao.insertAll(any()) } returns Unit
        coEvery { dao.getAll() } returns flowOf(youtubeVideos + otherVideos)

        val result = repository.getVideos(count = 2).first()

        assertEquals(2, result.size)
        coVerify(exactly = 1) { dao.insertAll(any()) }
    }

    @Test
    fun `getVideos should use cache when available and not refreshing`() = runTest {
        val cachedVideos = listOf(videoMock("1"), videoMock("2"))
        coEvery { dao.getVideoCount() } returns 2
        coEvery { dao.getAll() } returns flowOf(cachedVideos)

        val result = repository.getVideos(refresh = false).first()

        assertEquals(2, result.size)
        coVerify(exactly = 0) { youtubeApi.getPlaylistVideos(any(), any()) }
    }

    @Test
    fun `getVideo should return from dao`() = runTest {
        val expected = videoMock("123")
        coEvery { dao.getVideoById("123") } returns expected

        val result = repository.getVideo("123")

        assertEquals(expected, result)
    }

    private fun videoMock(
        id: String = "1",
        source: VideoSource = VideoSource.YOUTUBE
    ) = Video(
        id = id,
        title = "Video $id",
        duration = "0:00",
        thumbnailUrl = "http://thumb.com/$id",
        videoStreamingUrl = "http://video.com/$id",
        source = source
    )

    private fun Video.toApiModel() = VideoResponse(
        id = id,
        title = title,
        duration = duration,
        thumbnailUrl = thumbnailUrl,
        videoUrl = videoStreamingUrl,
        uploadTime = "",
        views = "",
        author = "",
        description = ""
    )

    private fun playlistResponse(
        nextPageToken: String? = null
    ) = YoutubePlaylistResponse(
        items = listOf(
            PlaylistItem(
                snippet = Snippet(
                    title = "Video",
                    thumbnails = Thumbnails(
                        medium = Thumbnail("thumb.jpg")
                    ),
                ),
                contentDetails = ContentDetails(videoId = "123")
            ),
        ),
        nextPageToken = nextPageToken
    )

    private fun videoDetailsResponse(duration: String) = VideoDetailsResponse(
        items = listOf(
            VideoDetailsItem(
                contentDetails = VideoContentDetails(duration)
            )
        )
    )

}