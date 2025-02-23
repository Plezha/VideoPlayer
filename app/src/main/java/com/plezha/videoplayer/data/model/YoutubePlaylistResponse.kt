package com.plezha.videoplayer.data.model

import com.google.gson.annotations.SerializedName

data class YoutubePlaylistResponse(
    @SerializedName("items") val items: List<PlaylistItem>,
    @SerializedName("nextPageToken") val nextPageToken: String?
)

data class PlaylistItem(
    @SerializedName("snippet") val snippet: Snippet,
    @SerializedName("contentDetails") val contentDetails: ContentDetails
)

data class Snippet(
    @SerializedName("title") val title: String,
    @SerializedName("thumbnails") val thumbnails: Thumbnails
)

data class Thumbnails(
    @SerializedName("medium") val medium: Thumbnail
)

data class Thumbnail(
    @SerializedName("url") val url: String
)

data class ContentDetails(
    @SerializedName("videoId") val videoId: String
)