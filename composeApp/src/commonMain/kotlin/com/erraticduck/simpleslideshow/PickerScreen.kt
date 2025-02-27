package com.erraticduck.simpleslideshow

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentHashSetOf
import kotlinx.collections.immutable.persistentListOf

@Composable
fun PickerScreen(
    images: ImmutableList<String> = persistentListOf(),
    selectedImageIndices: ImmutableSet<Int> = persistentHashSetOf(),
    onImageIndexSelected: (Int) -> Unit,
    audio: ImmutableList<String> = persistentListOf(),
    selectedAudioIndices: ImmutableSet<Int> = persistentHashSetOf(),
    onAudioIndexSelected: (Int) -> Unit,
    onImagesResult: (List<PlatformFile>) -> Unit,
    onAudioResult: (List<PlatformFile>) -> Unit,
    onDeleteClicked: () -> Unit,
    onSelectAll: () -> Unit,
    onStartMultiView: () -> Unit,
    onStartSlideshow: () -> Unit,
) {
    val selectionModeEnabled = selectedImageIndices.isNotEmpty() || selectedAudioIndices.isNotEmpty()
    Scaffold(
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
        topBar = {
            TopAppBar(
                title = { Text("Audio Slideshow") },
                windowInsets = AppBarDefaults.topAppBarWindowInsets,
                actions = {
                    if (selectionModeEnabled) {
                        TextButton(onSelectAll) {
                            Text("Select All")
                        }
                        TextButton(onDeleteClicked) {
                            Text("Delete")
                        }
                    } else {
                        TextButton(onStartMultiView, enabled = images.isNotEmpty()) {
                            Text("MultiView")
                        }
                        TextButton(onStartSlideshow, enabled = images.isNotEmpty()) {
                            Text("Slideshow")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->

        @Composable
        fun ColumnScope.ImagePicker() {
            ImagePicker(
                images,
                selectedImageIndices,
                onImageIndexSelected,
                selectionModeEnabled,
                onImagesResult
            )
        }

        @Composable
        fun ColumnScope.AudioPicker() {
            AudioPicker(
                audio,
                selectedAudioIndices,
                onAudioIndexSelected,
                selectionModeEnabled,
                onAudioResult
            )
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (maxWidth < 600.dp) {
                Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                    ImagePicker()
                    AudioPicker()
                }
            } else {
                Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        ImagePicker()
                    }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        AudioPicker()
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.ImagePicker(
    selectedImages: ImmutableList<String>,
    selectedIndices: ImmutableSet<Int>,
    onIndexSelected: (Int) -> Unit,
    selectionModeEnabled: Boolean,
    onImagesResult: (List<PlatformFile>) -> Unit,
) {
    FilePicker(
        pickerType = PickerType.Image,
        buttonText = "Pick Images",
        modifier = Modifier.weight(1f),
        uris = selectedImages,
        selected = selectedIndices,
        onIndexSelected = onIndexSelected,
        selectionModeEnabled = selectionModeEnabled,
        onFilesResult = onImagesResult
    )
}

@Composable
private fun ColumnScope.AudioPicker(
    selectedAudio: ImmutableList<String>,
    selectedIndices: ImmutableSet<Int>,
    onIndexSelected: (Int) -> Unit,
    selectionModeEnabled: Boolean,
    onAudioResult: (List<PlatformFile>) -> Unit,
) {
    FilePicker(
        pickerType = PickerType.File(listOf("mp3", "wav")),
        buttonText = "Pick Audio",
        modifier = Modifier.weight(1f),
        uris = selectedAudio,
        selected = selectedIndices,
        onIndexSelected = onIndexSelected,
        selectionModeEnabled = selectionModeEnabled,
        onFilesResult = onAudioResult
    )
}

@Composable
private fun FilePicker(
    pickerType: PickerType,
    buttonText: String,
    modifier: Modifier = Modifier,
    uris: ImmutableList<String> = persistentListOf(),
    selected: ImmutableSet<Int> = persistentHashSetOf(),
    onIndexSelected: (Int) -> Unit = {},
    selectionModeEnabled: Boolean = false,
    onFilesResult: (List<PlatformFile>) -> Unit
) {
    Column(modifier.padding(horizontal = 32.dp), Arrangement.Center, Alignment.CenterHorizontally) {
        val launcher = rememberFilePickerLauncher(
            type = pickerType,
            mode = PickerMode.Multiple(null)
        ) { files ->
            files?.let(onFilesResult)
        }

        Button(
            modifier = Modifier.padding(bottom = 8.dp),
            onClick = { launcher.launch() }
        ) {
            Text(buttonText)
        }

        val haptics = LocalHapticFeedback.current
        LazyColumn(modifier = Modifier.weight(1f, false)) {
            itemsIndexed(uris) { index, uri ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .combinedClickable(
                            onClick = {
                                if (selectionModeEnabled) {
                                    onIndexSelected(index)
                                }
                            },
                            onLongClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onIndexSelected(index)
                            }
                        )
                ) {
                    AnimatedVisibility(visible = selectionModeEnabled) {
                        Checkbox(
                            selected.contains(index),
                            onCheckedChange = { onIndexSelected(index) },
                        )
                    }
                    Text(
                        uri,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        overflow = TextOverflow.StartEllipsis,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}