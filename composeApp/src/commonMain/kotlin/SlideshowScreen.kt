import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList

@Composable
fun SlideshowScreen(
    images: ImmutableList<String>,
    onUpdateRunning: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shuffledIndices = rememberSaveable(images) { images.indices.shuffled().toList() }
    var currentImageIndex by rememberSaveable(images) { mutableStateOf(0) }

    var running by rememberSaveable { mutableStateOf(true) }
    val currentOnUpdateRunning by rememberUpdatedState(onUpdateRunning)

    fun previousImage() {
        currentImageIndex = (currentImageIndex - 1 + images.size) % images.size
    }

    fun nextImage() {
        currentImageIndex = (currentImageIndex + 1) % images.size
    }

    // When this screen leaves the composition, notify slideshow stopped
    DisposableEffect(running) {
        currentOnUpdateRunning(running)
        onDispose {
            currentOnUpdateRunning(false)
        }
    }

    Scaffold(
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
        modifier = modifier.background(Color.Black),
    ) { innerPadding ->

        // Image with transitions
        AnimatedContent(
            targetState = currentImageIndex,
            transitionSpec = {
                fadeIn(animationSpec = tween(1000)).togetherWith(
                    fadeOut(animationSpec = tween(1000))
                )
            }
        ) { targetIndex ->
            var animationState by remember { mutableStateOf(AnimationState()) }
            if (running) {
                LaunchedEffect(null) {
                    SlideshowAnimations.entries.random().animate {
                        animationState = it
                    }
                    nextImage()
                }
            } else {
                animationState = AnimationState()
            }

            ParallaxImage(
                imageUri = images[shuffledIndices[targetIndex]],
                animationState = animationState,
                modifier = Modifier.clickable { running = !running },
            )
        }

        // Foreground content
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            BottomToolbar(
                visible = running,
                modifier = Modifier.align(Alignment.BottomCenter).padding(64.dp),
                onPrevious = { previousImage() },
                onNext = { nextImage() },
            )
        }
    }

}

@Composable
private fun BottomToolbar(
    visible: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = !visible,
        modifier = modifier,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Row {
            Button(onClick = onPrevious) {
                Text("Previous")
            }
            Spacer(Modifier.width(8.dp))
            Button(onClick = onNext) {
                Text("Next")
            }
        }
    }
}