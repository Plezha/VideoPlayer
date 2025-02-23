package com.plezha.videoplayer.ui.videolist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.plezha.videoplayer.R
import com.plezha.videoplayer.data.model.Video
import com.plezha.videoplayer.ui.common.ErrorMessage
import com.plezha.videoplayer.ui.common.LoadingIndicator


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoListScreen(
    viewModel: VideoListViewModel = hiltViewModel(),
    onVideoClick: (String) -> Unit
) {
    val state = viewModel.state.collectAsState()
    val refreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        modifier = Modifier.fillMaxSize(),
        state = refreshState,
        onRefresh = { viewModel.loadVideos(true) },
        isRefreshing = state.value.isLoading
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (state.value.isLoading) {
                LoadingIndicator()
            } else if (state.value.error != null) {
                ErrorMessage(
                    message = state.value.error!!,
                    onRetry = { viewModel.loadVideos() }
                )
            } else {
                VideoList(
                    modifier = Modifier.fillMaxSize(),
                    videos = state.value.videos!!,
                    onVideoClick = onVideoClick
                )
            }
        }
    }
}

@Composable
fun VideoList(
    modifier: Modifier = Modifier,
    videos: List<Video>,
    onVideoClick: (String) -> Unit
) {
    LazyColumn(modifier = modifier) {
        videos.forEachIndexed { videoIndex, video ->
            item {
                key(video.id) {
                    VideoItem(
                        modifier = Modifier.padding(
                            bottom = if (videoIndex == videos.lastIndex) 0.dp else 8.dp
                        ),
                        video = video
                    ) {
                        onVideoClick(it.id)
                    }
                }
            }
        }
    }
}

@Composable
fun VideoItem(
    modifier: Modifier,
    video: Video,
    onClick: (Video) -> Unit
) {
    Column (
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(video) }
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            AsyncImage(
                model = video.thumbnailUrl,
                placeholder = painterResource(R.drawable.video_placeholder),
                error = painterResource(R.drawable.baseline_error_outline_24),
                contentDescription = null,
                contentScale = ContentScale.FillHeight,
                modifier = Modifier.fillMaxWidth().aspectRatio(16f/9f)
            )
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    modifier = Modifier.padding(4.dp),
                    text = video.duration,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
        Text(
            text = video.title,
            modifier = Modifier.padding(8.dp),
            style = MaterialTheme.typography.titleMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}