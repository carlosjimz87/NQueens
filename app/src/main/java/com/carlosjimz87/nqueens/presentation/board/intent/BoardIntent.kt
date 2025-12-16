package com.carlosjimz87.nqueens.presentation.board.intent

import com.carlosjimz87.rules.model.Cell

sealed interface BoardIntent {
    data class SetBoardSize(val size: Int) : BoardIntent
    data class ClickCell(val cell: Cell) : BoardIntent
    data object ResetGame : BoardIntent

    data object EnterWinAnimation : BoardIntent
    data object WinAnimationFinished : BoardIntent
}