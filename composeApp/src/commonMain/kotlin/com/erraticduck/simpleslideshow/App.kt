package com.erraticduck.simpleslideshow

import absolutePath
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.erraticduck.simpleslideshow.multiview.MultiViewScreen
import com.erraticduck.simpleslideshow.slideshow.SlideshowScreen

enum class Screens {
    Home,
    MultiView,
    Slideshow
}

@Composable
fun App(
    viewModel: SlideShowViewModel = viewModel { SlideShowViewModel() },
    navController: NavHostController = rememberNavController(),
    onToggleImmersive: (Boolean) -> Unit = {},
    onToggleAudio: (Boolean) -> Unit = {},
) {
    MaterialTheme(
        colors = darkColors()
    ) {
        val uiState by viewModel.uiState.collectAsState()
        DisposableEffect(navController) {
            val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
                destination.route?.let {
                    when (Screens.valueOf(it)) {
                        Screens.Home -> onToggleAudio(false)
                        Screens.MultiView,
                        Screens.Slideshow -> onToggleAudio(true)
                    }
                }
            }
            navController.addOnDestinationChangedListener(listener)
            onDispose {
                navController.removeOnDestinationChangedListener(listener)
            }
        }

        NavHost(
            navController = navController,
            startDestination = Screens.Home.name,
        ) {
            composable(Screens.Home.name) {
                onToggleImmersive(false)
                PickerScreen(
                    images = uiState.images,
                    selectedImageIndices = uiState.selectedImageIndices,
                    onImageIndexSelected = { viewModel.toggleImageSelection(it) },
                    audio = uiState.audio,
                    selectedAudioIndices = uiState.selectedAudioIndices,
                    onAudioIndexSelected = { viewModel.toggleAudioSelection(it) },
                    onImagesResult = { imageFiles ->
                        viewModel.addImages(imageFiles.mapNotNull { it.absolutePath() })
                    },
                    onAudioResult = { audioFiles ->
                        viewModel.addAudio(audioFiles.mapNotNull { it.absolutePath() })
                    },
                    onDeleteClicked = {
                        viewModel.removeSelectedImages()
                        viewModel.removeSelectedAudio()
                    },
                    onSelectAll = { viewModel.selectAll() },
                    onStartMultiView = {
                        if (uiState.images.isNotEmpty()) {
                            navController.navigate(Screens.MultiView.name)
                        }
                    },
                    onStartSlideshow = {
                        if (uiState.images.isNotEmpty()) {
                            navController.navigate(Screens.Slideshow.name)
                        }
                    }
                )
            }
            composable(Screens.MultiView.name) {
                MultiViewScreen(uiState.images, onToggleImmersive)
            }
            composable(Screens.Slideshow.name) {
                SlideshowScreen(uiState.images, onToggleImmersive)
            }
        }
    }
}