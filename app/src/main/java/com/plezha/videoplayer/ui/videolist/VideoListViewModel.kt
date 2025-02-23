package com.plezha.videoplayer.ui.videolist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plezha.videoplayer.data.YoutubeRepository
import com.plezha.videoplayer.data.model.Video
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class VideoListViewModel @Inject constructor(
    private val repo: YoutubeRepository
) : ViewModel() {
    private val _state = MutableStateFlow<VideoListState>(VideoListState(isLoading = true))
    val state: StateFlow<VideoListState> = _state.asStateFlow()

    init {
        loadVideos()
    }

    fun loadVideos(refresh: Boolean = false) {
        viewModelScope.launch {
            _state.value = VideoListState(isLoading = true)
            try {
                withContext(Dispatchers.IO) {
                    repo.getVideos(
                        refresh = refresh
                    ).collect { videos ->
                        _state.value = VideoListState(videos = videos)
                    }
                }
            } catch (e: Exception) {
                _state.value = VideoListState(error = e.message ?: "Unknown error")
            }
        }
    }
}

data class VideoListState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val videos: List<Video>? = null
)