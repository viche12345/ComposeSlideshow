package com.erraticduck.simpleslideshow

import androidx.lifecycle.ViewModel
import kotlinx.collections.immutable.persistentHashSetOf
import kotlinx.collections.immutable.toPersistentHashSet
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SlideShowViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SlideShowUiState())
    val uiState = _uiState.asStateFlow()

    fun addImages(images: List<String>) {
        _uiState.update { it.copy(images = (it.images + images).toPersistentList()) }
    }

    fun removeSelectedImages() {
        _uiState.update { currentState ->
            currentState.copy(
                images = currentState.images.toMutableList().apply {
                    currentState.selectedImageIndices.sortedDescending().forEach { removeAt(it) }
                }.toPersistentList(),
                selectedImageIndices = persistentHashSetOf()
            )
        }
    }

    fun toggleImageSelection(index: Int) {
        _uiState.update { it.copy(selectedImageIndices = it.selectedImageIndices.toggle(index).toPersistentHashSet()) }
    }

    fun addAudio(audio: List<String>) {
        _uiState.update { it.copy(audio = (it.audio + audio).toPersistentList()) }
    }

    fun removeSelectedAudio() {
        _uiState.update { currentState ->
            currentState.copy(
                audio = currentState.audio.toMutableList().apply {
                    currentState.selectedAudioIndices.sortedDescending().forEach { removeAt(it) }
                }.toPersistentList(),
                selectedAudioIndices = persistentHashSetOf()
            )
        }
    }

    fun toggleAudioSelection(index: Int) {
        _uiState.update { it.copy(selectedAudioIndices = it.selectedAudioIndices.toggle(index).toPersistentHashSet()) }
    }

    fun selectAll() {
        _uiState.update { currentState ->
            if (currentState.selectedImageIndices.size == currentState.images.size &&
                currentState.selectedAudioIndices.size == currentState.audio.size
            ) {
                currentState.copy(
                    selectedImageIndices = persistentHashSetOf(),
                    selectedAudioIndices = persistentHashSetOf()
                )
            } else {
                currentState.copy(
                    selectedImageIndices = currentState.images.indices.toPersistentHashSet(),
                    selectedAudioIndices = currentState.audio.indices.toPersistentHashSet()
                )
            }
        }
    }

    private fun Set<Int>.toggle(index: Int): Set<Int> =
        if (index in this) {
            this - index
        } else {
            this + index
        }

}