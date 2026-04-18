package com.erraticduck.simpleslideshow.multiview

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import com.github.panpf.zoomimage.CoilZoomAsyncImage
import com.github.panpf.zoomimage.rememberCoilZoomState
import io.github.oikvpqya.compose.fastscroller.HorizontalScrollbar
import io.github.oikvpqya.compose.fastscroller.defaultScrollbarStyle
import io.github.oikvpqya.compose.fastscroller.rememberScrollbarAdapter
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
    ) { paddingValues ->
        ImageRow(
            images = images,
            scaffoldPaddingValues = paddingValues,
        ) {
            isImmersive = !isImmersive
        }
    }
}

@Composable
fun ImageRow(
    images: ImmutableList<String>,
    modifier: Modifier = Modifier,
    scaffoldPaddingValues: PaddingValues,
    onImageTap: (Int) -> Unit = {},
) {
    BoxWithConstraints(modifier) {
        // Minimum width any image can be resized to
        val minWidthDp = 240.dp
        val density = LocalDensity.current
        val containerHeightPx = with(density) { maxHeight.toPx() }

        // visibleWidths: the user-facing widths, adjusted by divider dragging.
        // Starts at minWidthDp and updates to the natural image width once intrinsic sizes resolve.
        val visibleWidths = remember {
            List(images.size) { minWidthDp }.toMutableStateList()
        }

        // zoomContainerWidths: stable inner widths for CoilZoomAsyncImage.
        // These do NOT change during divider drag, preventing the zoom library from resetting
        // its zoom state (which it does whenever its container size changes).
        val zoomContainerWidths = remember { List(images.size) { minWidthDp }.toMutableStateList() }

        val lazyRowState = rememberLazyListState()
        LazyRow(
            modifier = Modifier.align(Alignment.Center),
            state = lazyRowState,
        ) {
            itemsIndexed(images) { index, uri ->
                val zoomState = rememberCoilZoomState()
                var sizeResolved by remember { mutableStateOf(false) }

                // contentBaseDisplayRect is the image's display rect at base zoom (before any
                // user pinch-zoom), so it stays stable regardless of zoom level.
                val baseImageWidth = zoomState.zoomable.contentBaseDisplayRect.width

                // Cap the visible width to the image's base display width so that expanding
                // the divider never reveals black space beyond the image edges.
                // Before the zoom library has computed the display rect, fall back to
                // zoomContainerWidths which equals the natural width after resolution.
                val maxVisibleWidth = if (baseImageWidth > 0) {
                    with(density) { baseImageWidth.toDp() }
                } else {
                    zoomContainerWidths[index]
                }

                // The actual outer box width: the user's requested width, but never exceeding
                // the image's base display width (no black space beyond image edges).
                val outerWidth = visibleWidths[index].coerceAtMost(maxVisibleWidth)

                // Outer Box: sized to outerWidth with clipToBounds so the inner zoom image
                // (which may be wider) is clipped to the visible area.
                Box(Modifier.width(outerWidth).fillMaxHeight().clipToBounds()) {
                    // Inner CoilZoomAsyncImage: uses the stable zoomContainerWidths so the
                    // zoom library's container size doesn't change during divider drag.
                    CoilZoomAsyncImage(
                        model = uri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        scrollBar = null,
                        zoomState = zoomState,
                        modifier = Modifier.fillMaxHeight().width(zoomContainerWidths[index]),
                        onTap = { onImageTap(index) },
                        onState = { state ->
                            // Once the image loads successfully, compute its natural width
                            // (the width at which it exactly fills the container height with
                            // no cropping) and update both width lists.
                            if (state is AsyncImagePainter.State.Success && !sizeResolved) {
                                sizeResolved = true
                                val size = state.painter.intrinsicSize
                                if (size != Size.Unspecified) {
                                    val aspectRatio = size.width / size.height
                                    // Natural width = container height × aspect ratio
                                    val naturalWidthDp = with(density) { (containerHeightPx * aspectRatio).toDp() }
                                    // Ensure we never go below the minimum
                                    val newWidth = naturalWidthDp.coerceAtLeast(minWidthDp)
                                    visibleWidths[index] = newWidth
                                    zoomContainerWidths[index] = newWidth
                                }
                            }
                        },
                    )

                    // Image divider at start (between previous image and this one).
                    // Dragging right: expands previous image, shrinks this one.
                    // Dragging left: shrinks previous image, expands this one.
                    if (index > 0) {
                        Box(
                            Modifier.fillMaxHeight().width(24.dp).pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    val dragAmountDp = dragAmount.x.toDp()

                                    // Adjust the previous image's width, clamped between
                                    // minWidthDp and its natural width (zoomContainerWidths)
                                    // so it never grows past the image's actual content.
                                    val oldWidth = visibleWidths[index - 1]
                                    visibleWidths[index - 1] = (oldWidth + dragAmountDp).coerceIn(minWidthDp, zoomContainerWidths[index - 1])

                                    // Only shrink/expand this image by the actual amount the
                                    // previous image changed (after clamping). This prevents
                                    // the neighbor from shrinking when the expanding image
                                    // has already hit its max.
                                    val actualChange = visibleWidths[index - 1] - oldWidth
                                    if (actualChange != 0.dp) {
                                        visibleWidths[index] = (visibleWidths[index] - actualChange).coerceAtLeast(minWidthDp)
                                    }
                                }
                            }
                        )
                    }

                    // Image divider at end (between this image and the next one).
                    // Dragging right: expands this image, shrinks next one.
                    // Dragging left: shrinks this image, expands next one.
                    if (index < images.size - 1) {
                        Box(
                            Modifier.align(Alignment.TopEnd).fillMaxHeight().width(24.dp).pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    val dragAmountDp = dragAmount.x.toDp()

                                    // Adjust this image's width, clamped between minWidthDp
                                    // and its natural width so it never grows past the
                                    // image's actual content.
                                    val oldWidth = visibleWidths[index]
                                    visibleWidths[index] = (oldWidth + dragAmountDp).coerceIn(minWidthDp, zoomContainerWidths[index])

                                    // Only shrink/expand the next image by the actual amount
                                    // this image changed (after clamping).
                                    val actualChange = visibleWidths[index] - oldWidth
                                    if (actualChange != 0.dp) {
                                        visibleWidths[index + 1] = (visibleWidths[index + 1] - actualChange).coerceAtLeast(minWidthDp)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        HorizontalScrollbar(
            modifier = Modifier.align(Alignment.BottomCenter).padding(scaffoldPaddingValues).padding(horizontal = 16.dp),
            adapter = rememberScrollbarAdapter(lazyRowState),
            style = defaultScrollbarStyle().copy(thickness = 16.dp),
        )
    }
}