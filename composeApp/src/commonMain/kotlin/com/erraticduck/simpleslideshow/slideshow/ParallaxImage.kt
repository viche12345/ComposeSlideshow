package com.erraticduck.simpleslideshow.slideshow

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.compose.rememberConstraintsSizeResolver
import coil3.request.ImageRequest

@Composable
fun ParallaxImage(
    imageUri: String,
    animationState: AnimationState,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        val sizeResolver = rememberConstraintsSizeResolver()
        var foregroundImageSize by remember { mutableStateOf(IntSize(0, 0)) }
        val painter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .data(imageUri)
                .size(sizeResolver)
                .build(),
        )

        // Blurred background image
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .blur(24.dp)
                .graphicsLayer {
                    val parallaxScale = .33f * (animationState.scale - 1) + 1
                    val parallaxTranslationX = .33f * animationState.translationX
                    val parallaxTranslationY = .33f * animationState.translationY

                    scaleX = parallaxScale
                    scaleY = parallaxScale
                    translationX = parallaxTranslationX * foregroundImageSize.width
                    translationY = parallaxTranslationY * foregroundImageSize.height
                },
            contentScale = ContentScale.Crop,
            alpha = .4f,
        )

        // Foreground image
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .then(sizeResolver.onSizeChanged { foregroundImageSize = it })
                .graphicsLayer {
                    scaleX = animationState.scale
                    scaleY = animationState.scale
                    translationX = animationState.translationX * foregroundImageSize.width
                    translationY = animationState.translationY * foregroundImageSize.height
                }
                .blur(0.01.dp),  // Apply slight blur to reduce aliasing
        )
    }
}