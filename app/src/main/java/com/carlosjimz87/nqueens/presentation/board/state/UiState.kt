package com.carlosjimz87.nqueens.presentation.board.state

import com.carlosjimz87.rules.model.BoardError

sealed class UiState {
    data object Idle : UiState()
    data object Loading : UiState()
    data class InvalidBoard(val message: String, val error: BoardError) : UiState()
}