import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.delay

@Composable
fun SlideshowScreen(
    images: ImmutableList<String>,
    onUpdateRunning: (Boolean) -> Unit,
) {
    val shuffledIndices = rememberSaveable(images) { images.indices.shuffled().toList() }
    var currentImageIndex by rememberSaveable(images) { mutableStateOf(0) }

    var running by rememberSaveable { mutableStateOf(true) }
    val currentOnUpdateRunning by rememberUpdatedState(onUpdateRunning)

    fun previousImage() {
        currentImageIndex = (currentImageIndex - 1 + images.size) % images.size
    }

    fun advanceImage() {
        currentImageIndex = (currentImageIndex + 1) % images.size
    }

    // Slideshow automatic advancement
    LaunchedEffect(running) {
        currentOnUpdateRunning(running)
        while (running) {
            delay(4000)
            advanceImage()
        }
    }

    // When this screen leaves the composition, notify slideshow stopped
    DisposableEffect(null) {
        onDispose {
            currentOnUpdateRunning(false)
        }
    }

    Scaffold(
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentImageIndex,
            transitionSpec = {
                fadeIn(animationSpec = tween(1000)).togetherWith(
                    fadeOut(animationSpec = tween(1000))
                )
            }
        ) { targetIndex ->
            AsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(images[shuffledIndices[targetIndex]])
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().clickable { running = !running }
            )
        }

        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            AnimatedVisibility(
                visible = !running,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 64.dp),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row {
                        Button(onClick = { previousImage() }) {
                            Text("Previous")
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = { advanceImage() }) {
                            Text("Next")
                        }
                    }
                }
            }
        }
    }

}