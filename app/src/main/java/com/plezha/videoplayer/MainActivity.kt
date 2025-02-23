package com.plezha.videoplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.activity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.plezha.videoplayer.ui.theme.VideoPlayerTheme
import com.plezha.videoplayer.ui.videolist.VideoListScreen
import com.plezha.videoplayer.ui.videoplayer.VideoPlayerActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VideoPlayerTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { padding ->
                    VideoPlayerNavigation(modifier = Modifier.padding(padding))
                }
            }
        }
    }
}

@Composable
fun VideoPlayerNavigation(modifier: Modifier) {
    val navController = rememberNavController()

    NavHost(modifier = modifier, navController = navController, startDestination = "list") {
        composable("list") {
            VideoListScreen { video ->
                navController.navigate("player/${video}")
            }
        }

        activity("player/{videoId}") {
            argument("videoId") { type = NavType.StringType }
            activityClass = VideoPlayerActivity::class
        }
    }
}