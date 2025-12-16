package com.carlosjimz87.nqueens.presentation.board.state

import com.carlosjimz87.rules.model.BoardError

sealed interface UiState {
    data object Idle : UiState
    data object Loading : UiState
    data class InvalidBoard(val error: BoardError) : UiState
}