import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
fun PickerScreen(
    selectedImages: ImmutableList<String> = persistentListOf(),
    selectedAudio: ImmutableList<String> = persistentListOf(),
    onImagesResult: (List<PlatformFile>) -> Unit,
    onAudioResult: (List<PlatformFile>) -> Unit,
    onStartSlideshow: () -> Unit,
) {
    Scaffold(
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
        topBar = {
            TopAppBar(
                title = { Text("Audio Slideshow") },
                windowInsets = AppBarDefaults.topAppBarWindowInsets,
                actions = {
                    TextButton(onStartSlideshow, enabled = selectedImages.isNotEmpty()) {
                        Text("START")
                    }
                }
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (maxWidth < 600.dp) {
                Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                    ImagePicker(selectedImages, onImagesResult)
                    AudioPicker(selectedAudio, onAudioResult)
                }
            } else {
                Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        ImagePicker(selectedImages, onImagesResult)
                    }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        AudioPicker(selectedAudio, onAudioResult)
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.ImagePicker(selectedImages: ImmutableList<String>, onImagesResult: (List<PlatformFile>) -> Unit) {
    FilePicker(
        pickerType = PickerType.File(listOf("jpg", "jpeg")),
        buttonText = "Pick Images",
        modifier = Modifier.weight(1f),
        selected = selectedImages,
        onFilesResult = onImagesResult
    )
}

@Composable
private fun ColumnScope.AudioPicker(selectedAudio: ImmutableList<String>, onAudioResult: (List<PlatformFile>) -> Unit) {
    FilePicker(
        pickerType = PickerType.File(listOf("mp3", "wav")),
        buttonText = "Pick Audio",
        modifier = Modifier.weight(1f),
        selected = selectedAudio,
        onFilesResult = onAudioResult
    )
}

@Composable
private fun FilePicker(
    pickerType: PickerType,
    buttonText: String,
    modifier: Modifier = Modifier,
    selected: ImmutableList<String> = persistentListOf(),
    onFilesResult: (List<PlatformFile>) -> Unit
) {
    Column(modifier.padding(32.dp), Arrangement.Center, Alignment.CenterHorizontally) {
        val launcher = rememberFilePickerLauncher(
            type = pickerType,
            mode = PickerMode.Multiple
        ) { files ->
            files?.let(onFilesResult)
        }

        Button(
            modifier = Modifier.padding(bottom = 8.dp),
            onClick = { launcher.launch() }
        ) {
            Text(buttonText)
        }

        LazyColumn(modifier = Modifier.weight(1f, false)) {
            items(selected) { image ->
                Text(image, Modifier.padding(vertical = 2.dp), style = MaterialTheme.typography.caption)
            }
        }
    }
}