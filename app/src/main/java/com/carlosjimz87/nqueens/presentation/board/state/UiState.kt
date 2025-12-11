package com.carlosjimz87.nqueens.presentation.board.state

import com.carlosjimz87.nqueens.domain.error.BoardError

sealed class UiState {
    data class Loading(val message: String = "Loading...") : UiState()
    data class BoardInvalid(val message: String, val error: BoardError = BoardError.NoError) : UiState()
}