package com.plezha.videoplayer.ui.videoplayer

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.view.OrientationEventListener
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.plezha.videoplayer.data.model.Video
import com.plezha.videoplayer.data.model.VideoSource
import com.plezha.videoplayer.ui.common.ErrorMessage
import com.plezha.videoplayer.ui.common.LoadingIndicator
import com.plezha.videoplayer.ui.theme.VideoPlayerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VideoPlayerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VideoPlayerTheme {
                val viewModel: VideoPlayerViewModel = hiltViewModel()
                val state = viewModel.state.collectAsState().value
                Scaffold { padding ->
                    VideoPlayerScreen(
                        modifier = Modifier.padding(padding),
                        uiState = state,
                        onRetry = viewModel::loadVideo,
                        onFullscreenChanged = {
                            if (it) {
                                viewModel.enterFullscreen()
                            } else {
                                viewModel.exitFullscreen()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun VideoPlayerScreen(
    modifier: Modifier = Modifier,
    uiState: VideoPlayerState,
    onRetry: () -> Unit,
    onFullscreenChanged: (Boolean) -> Unit
) {
    when (uiState) {
        is VideoPlayerState.Error ->
            ErrorMessage(uiState.message, onRetry)
        is VideoPlayerState.Loading -> LoadingIndicator()
        is VideoPlayerState.Success -> VideoPlayerScreen(modifier, uiState, onFullscreenChanged)
    }
}

@SuppressLint("SourceLockedOrientationActivity")
@Composable
private fun VideoPlayerScreen(
    modifier: Modifier = Modifier,
    uiState: VideoPlayerState.Success,
    onFullscreenChanged: (Boolean) -> Unit
) {
    val context = LocalActivity.current!!

    val video = uiState.video
    val isFullscreen = uiState.isVideoFullscreen
    
    val configuration = LocalConfiguration.current
    val orientation = configuration.orientation

    val orientationEventListener = remember {
        object : OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                val isPortrait = (orientation > 340 || orientation < 20 || orientation in 160..200)
                val isLandscape = (orientation in 250..290 || orientation in 70..110)
                if (
                    (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE && isLandscape)
                    || (configuration.orientation == Configuration.ORIENTATION_PORTRAIT && isPortrait)
                    && context.requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_USER
                ) {
                    context.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER
                }
            }
        }
    }
    LaunchedEffect(isFullscreen) {
        if (isFullscreen) {
            context.hideSystemUi()
            context.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            context.showSystemUi()
            context.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    LaunchedEffect(orientation) {
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            onFullscreenChanged(true)
        } else {
            onFullscreenChanged(false)
        }
    }

    DisposableEffect(Unit) {
        orientationEventListener.enable()
        onDispose { orientationEventListener.disable() }
    }

    Box(
        modifier = if (isFullscreen) {
            modifier
                .fillMaxSize()
        } else {
            modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
        },
        contentAlignment = Alignment.TopCenter,
    ) {
        when (video.source) {
            VideoSource.YOUTUBE -> WebviewVideoPlayer(video, onFullscreenChanged)
            VideoSource.OTHER -> ExoplayerVideoPlayer(video, isFullscreen, onFullscreenChanged)
        }
    }
}

private fun Context.hideSystemUi() {
    val activity = this.findActivity() ?: return
    val window = activity.window ?: return
    WindowCompat.setDecorFitsSystemWindows(window, false)
    WindowInsetsControllerCompat(window, window.decorView).apply {
        hide(WindowInsetsCompat.Type.systemBars())
        systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}

private fun Context.showSystemUi() {
    val activity = this.findActivity() ?: return
    val window = activity.window ?: return
    WindowCompat.setDecorFitsSystemWindows(window, true)
    WindowInsetsControllerCompat(
        window,
        window.decorView
    ).show(WindowInsetsCompat.Type.systemBars())
}

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@OptIn(UnstableApi::class)
@Composable
private fun ExoplayerVideoPlayer(
    video: Video,
    isFullscreen: Boolean,
    onFullscreenChanged: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(video.videoStreamingUrl))
            prepare()
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    val playerView = remember {
        PlayerView(context).apply {
            setFullscreenButtonClickListener {
                onFullscreenChanged(it)
            }
            this.player = exoPlayer
            useController = true
        }
    }
    LaunchedEffect(isFullscreen) {
        playerView.setFullscreenButtonState(isFullscreen)
    }

    AndroidView(
        factory = {
            playerView
        }
    )
}

@Composable
private fun WebviewVideoPlayer(
    video: Video,
    onFullscreenChanged: (Boolean) -> Unit
) {
    val context = LocalContext.current

    val htmlVideo =
        "<body style=\"margin: 0; padding: 0\"><iframe width=\"100%\" height=\"100%\" src=\"${video.videoStreamingUrl}\" title=\"YouTube video player\" frameborder=\"0\" allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share\" allowfullscreen></iframe></body>"

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams =
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                settings.apply {
                    javaScriptEnabled = true
                }
                webChromeClient = FullscreenableWebChromeClient(
                    enterFullscreen = {
                        onFullscreenChanged(true)
                        Toast.makeText(context, "Почему-то у меня ломается WebView, когда я перевожу его в полный экран. Я с этим не справился за 6 часов", Toast.LENGTH_LONG).show()
                    },
                    exitFullscreen = {
                        onFullscreenChanged(false)
                    }
                )
                loadData(htmlVideo, "text/html", "utf-8")
            }
        },
        modifier = Modifier
            .fillMaxWidth()
    )
}