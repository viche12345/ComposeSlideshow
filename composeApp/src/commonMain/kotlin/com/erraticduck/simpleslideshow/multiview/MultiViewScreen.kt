package com.erraticduck.simpleslideshow.multiview

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.panpf.zoomimage.CoilZoomAsyncImage
import kotlinx.collections.immutable.ImmutableList

@Composable
fun MultiViewScreen(
    images: ImmutableList<String>,
    onToggleImmersive: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isImmersive by rememberSaveable { mutableStateOf(true) }
    val currentOnToggleImmersive by rememberUpdatedState(onToggleImmersive)
    DisposableEffect(isImmersive) {
        currentOnToggleImmersive(isImmersive)
        onDispose {
            currentOnToggleImmersive(false)
        }
    }

    Scaffold(
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
        modifier = modifier.background(Color.Black),
    ) {
        ImageRow(images = images) {
            isImmersive = !isImmersive
        }
    }
}

@Composable
fun ImageRow(
    images: ImmutableList<String>,
    modifier: Modifier = Modifier,
    onImageTap: (Int) -> Unit = {},
) {
    BoxWithConstraints(modifier) {
        val minWidthDp = 240.dp
        val widths = rememberSaveable(
            saver = listSaver(
                save = { stateList -> stateList.map { it.value } },
                restore = { it.map(::Dp).toMutableStateList() },
            )
        ) {
            mutableStateListOf<Dp>().apply {
                images.map { add((maxWidth.value / images.size).dp.coerceAtLeast(minWidthDp)) }
            }
        }

        // Display all images in a row, with the ability to resize each image by dragging a divider between them.
        LazyRow(Modifier.align(Alignment.Center)) {
            itemsIndexed(images) { index, uri ->
                Box {
                    CoilZoomAsyncImage(
                        model = uri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        scrollBar = null,
                        modifier = Modifier.fillMaxHeight().width(widths[index]),
                        onTap = { onImageTap(index) },
                    )

                    // Image divider at start
                    if (index > 0) {
                        Box(
                            Modifier.fillMaxHeight().width(24.dp).pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    val dragAmountDp = dragAmount.x.toDp()
                                    // First, adjust the previous image's width.
                                    widths[index - 1] = (widths[index - 1] + dragAmountDp).coerceAtLeast(minWidthDp)

                                    // Then, after adjusting, if the previous image's width is still greater than the minimum width,
                                    // adjust the current image's width as well.
                                    if (widths[index - 1] > minWidthDp) {
                                        widths[index] = (widths[index] - dragAmountDp).coerceAtLeast(minWidthDp)
                                    }
                                }
                            }
                        )
                    }

                    // Image divider at end
                    if (index < images.size - 1) {
                        Box(
                            Modifier.align(Alignment.TopEnd).fillMaxHeight().width(24.dp).pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    val dragAmountDp = dragAmount.x.toDp()
                                    // First, adjust the current image's width.
                                    widths[index] = (widths[index] + dragAmountDp).coerceAtLeast(minWidthDp)

                                    // Then, after adjusting, if the current image's width is still greater than the minimum width,
                                    // adjust the next image's width as well.
                                    if (widths[index] > minWidthDp) {
                                        widths[index + 1] = (widths[index + 1] - dragAmountDp).coerceAtLeast(minWidthDp)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}