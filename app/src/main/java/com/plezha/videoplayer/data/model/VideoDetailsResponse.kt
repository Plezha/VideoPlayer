package com.plezha.videoplayer.data.model

import com.google.gson.annotations.SerializedName

data class VideoDetailsResponse(
    @SerializedName("items") val items: List<VideoDetailsItem>
)

data class VideoDetailsItem(
    @SerializedName("contentDetails") val contentDetails: VideoContentDetails
)

data class VideoContentDetails(
    @SerializedName("duration") val duration: String
)