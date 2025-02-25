package com.plezha.videoplayer.ui.videoplayer

import androidx.lifecycle.SavedStateHandle
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
class VideoPlayerViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repo: YoutubeRepository
) : ViewModel() {
    private val _state = MutableStateFlow<VideoPlayerState>(VideoPlayerState.Loading)
    val state: StateFlow<VideoPlayerState> = _state.asStateFlow()

    init {
        loadVideo()
    }

    fun loadVideo() {
        viewModelScope.launch {
            _state.value = VideoPlayerState.Loading
            try {
                withContext(Dispatchers.IO) {
                    _state.value = VideoPlayerState.Success(
                        video = repo.getVideo(
                            id = savedStateHandle.get<String>("videoId")!!
                        )
                    )
                }
            } catch (e: Exception) {
                _state.value = VideoPlayerState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun enterFullscreen() {
        val state = _state.value
        if (state is VideoPlayerState.Success && !state.isVideoFullscreen) {
            _state.value = (_state.value as VideoPlayerState.Success)
                .copy(isVideoFullscreen = true)
        }
    }

    fun exitFullscreen() {
        val state = _state.value
        if (state is VideoPlayerState.Success && state.isVideoFullscreen) {
            _state.value = (_state.value as VideoPlayerState.Success)
                .copy(isVideoFullscreen = false)
        }
    }
}

sealed interface VideoPlayerState {
    data object Loading : VideoPlayerState
    data class Error(val message: String): VideoPlayerState
    data class Success(
        val isVideoFullscreen: Boolean = false,
        val video: Video
    ): VideoPlayerState
}
