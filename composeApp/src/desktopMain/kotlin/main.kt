import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.erraticduck.simpleslideshow.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Audio Slideshow",
    ) {
        App()
    }
}