import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween

enum class SlideshowAnimations(
    val initialValue: Float,
    val targetValue: Float,
) {

    SLIDE_LEFT(.015f, -.015f) {
        override suspend fun animate(block: (AnimationState) -> Unit) {
            androidx.compose.animation.core.animate(initialValue, targetValue, animationSpec = animationSpec) { value, _ ->
                println("value: $value")
                block(
                    AnimationState(
                        translationX = value,
                        scale = (.015f * 2) + 1
                    )
                )
            }
        }
    },

    SLIDE_RIGHT(-.015f, .015f) {
        override suspend fun animate(block: (AnimationState) -> Unit) {
            androidx.compose.animation.core.animate(initialValue, targetValue, animationSpec = animationSpec) { value, _ ->
                println("value: $value")
                block(
                    AnimationState(
                        translationX = value,
                        scale = (.015f * 2) + 1
                    )
                )
            }
        }
    },

    ZOOM_OUT(1.10f, 1f) {
        override suspend fun animate(block: (AnimationState) -> Unit) {
            androidx.compose.animation.core.animate(initialValue, targetValue, animationSpec = animationSpec) { value, _ ->
                block(AnimationState(scale = value))
            }
        }
    },

    ZOOM_OUT_POWERFUL(1.5f, 1f) {
        override suspend fun animate(block: (AnimationState) -> Unit) {
            androidx.compose.animation.core.animate(
                initialValue,
                targetValue,
                animationSpec = tween(4000, easing = CubicBezierEasing(.02f, .9f, .25f, .95f))
            ) { value, _ ->
                block(AnimationState(scale = value))
            }
        }
    },

    ZOOM_IN(1f, 1.10f) {
        override suspend fun animate(block: (AnimationState) -> Unit) {
            androidx.compose.animation.core.animate(initialValue, targetValue, animationSpec = animationSpec) { value, _ ->
                block(AnimationState(scale = value))
            }
        }
    };

    abstract suspend fun animate(block: (AnimationState) -> Unit)
}

data class AnimationState(
    val scale: Float = 1f,
    val translationX: Float = 0f,
    val translationY: Float = 0f,
)

private val animationSpec: AnimationSpec<Float> = tween(4000, easing = LinearOutSlowInEasing)