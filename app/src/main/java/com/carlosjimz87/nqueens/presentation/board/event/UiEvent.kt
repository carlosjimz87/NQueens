package com.carlosjimz87.nqueens.presentation.board.event

import com.carlosjimz87.nqueens.domain.model.Cell

sealed interface UiEvent {
    data class QueenPlaced(val cell: Cell) : UiEvent
    data class QueenRemoved(val cell: Cell) : UiEvent
}