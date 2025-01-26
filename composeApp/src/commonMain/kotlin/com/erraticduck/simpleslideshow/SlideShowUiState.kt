package com.erraticduck.simpleslideshow

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class SlideShowUiState(
    val images: ImmutableList<String> = persistentListOf(),
    val audio: ImmutableList<String> = persistentListOf(),
)