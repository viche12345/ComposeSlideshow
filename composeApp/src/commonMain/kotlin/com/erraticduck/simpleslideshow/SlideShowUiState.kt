package com.erraticduck.simpleslideshow

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentHashSetOf
import kotlinx.collections.immutable.persistentListOf

data class SlideShowUiState(
    val images: ImmutableList<String> = persistentListOf(),
    val selectedImageIndices: ImmutableSet<Int> = persistentHashSetOf(),
    val audio: ImmutableList<String> = persistentListOf(),
    val selectedAudioIndices: ImmutableSet<Int> = persistentHashSetOf(),
)