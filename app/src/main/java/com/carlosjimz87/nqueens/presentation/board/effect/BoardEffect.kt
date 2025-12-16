package com.carlosjimz87.nqueens.presentation.board.effect

import com.carlosjimz87.nqueens.presentation.audio.model.Sound

sealed interface BoardEffect {
    data class PlaySound(val sound: Sound) : BoardEffect
    data class ShowSnackbar(val message: String) : BoardEffect
}