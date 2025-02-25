package com.plezha.videoplayer.data.model

data class VideoResponse(
    var id: String,
    var title: String,
    var thumbnailUrl: String,
    var duration: String,
    var uploadTime: String,
    var views: String,
    var author: String,
    var videoUrl: String,
    var description: String,
)
