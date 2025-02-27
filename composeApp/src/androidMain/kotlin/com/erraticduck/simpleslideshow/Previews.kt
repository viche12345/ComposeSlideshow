package com.erraticduck.simpleslideshow

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel

@Preview
@Composable
fun PickerScreenPreview() {
    val viewModel = viewModel {
        SlideShowViewModel().apply {
            addImages(
                listOf(
                    "/path/path/path/path/path/path/path/path/path/path/path/path/path/path/path/to/image1.jpg",
                    "/path/to/image2.jpg",
                    "/path/to/image3.jpg",
                    "/path/to/image4.jpg",
                    "/path/to/image5.jpg",
                    "/path/to/image6.jpg",
                )
            )
            addAudio(
                listOf(
                    "/path/to/audio1.mp3",
                    "/path/to/audio2.mp3",
                    "/path/to/audio3.mp3",
                    "/path/to/audio4.mp3",
                    "/path/to/audio5.mp3",
                )
            )
        }
    }
    val state by viewModel.uiState.collectAsState()
    PickerScreen(
        images = state.images,
        selectedImageIndices = state.selectedImageIndices,
        onImageIndexSelected = { viewModel.toggleImageSelection(it) },
        audio = state.audio,
        selectedAudioIndices = state.selectedAudioIndices,
        onAudioIndexSelected = { viewModel.toggleAudioSelection(it) },
        onImagesResult = { },
        onAudioResult = { },
        onDeleteClicked = {
            viewModel.removeSelectedImages()
            viewModel.removeSelectedAudio()
        },
        onSelectAll = { viewModel.selectAll() },
        onStartMultiView = { },
        onStartSlideshow = { },
    )
}