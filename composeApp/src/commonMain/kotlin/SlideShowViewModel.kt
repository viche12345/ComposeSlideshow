import androidx.lifecycle.ViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SlideShowViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SlideShowUiState())
    val uiState = _uiState.asStateFlow()

    fun updateImagesIfNotEmpty(images: List<String>) {
        if (images.isNotEmpty()) {
            _uiState.update { it.copy(images = images.toPersistentList()) }
        }
    }

    fun updateAudioIfNotEmpty(audio: List<String>) {
        if (audio.isNotEmpty()) {
            _uiState.update { it.copy(audio = audio.toPersistentList()) }
        }
    }

}