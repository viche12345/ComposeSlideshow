import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.navigation.compose.rememberNavController
import com.erraticduck.simpleslideshow.App

fun main() = application {
    val navController = rememberNavController()
    Window(
        onCloseRequest = ::exitApplication,
        title = "Audio Slideshow",
        onPreviewKeyEvent = {
            if (it.key == Key.Escape && it.type == KeyEventType.KeyDown) {
                navController.popBackStack()
                true
            } else {
                false
            }
        }
    ) {
        App(
            navController = navController,
        )
    }
}