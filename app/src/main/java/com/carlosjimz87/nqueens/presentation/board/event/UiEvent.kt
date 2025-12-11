package com.carlosjimz87.nqueens.presentation.board.event

sealed class UiEvent {
    data object QueenPlaced : UiEvent()
}