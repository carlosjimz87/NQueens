package com.carlosjimz87.nqueens.presentation.board.state

import com.carlosjimz87.nqueens.data.model.LatestRank
import com.carlosjimz87.rules.model.BoardError
import com.carlosjimz87.rules.model.Cell
import com.carlosjimz87.rules.model.Conflicts
import com.carlosjimz87.rules.model.GameState

enum class BoardPhase {
    Normal,
    WinAnimating,
    WinFrozen
}

data class BoardState(
    val isLoading: Boolean = false,
    val boardSize: Int? = null,
    val gameState: GameState? = null,
    val elapsedMillis: Long = 0L,
    val latestRank: LatestRank? = null,
    val boardPhase: BoardPhase = BoardPhase.Normal,
    val error: BoardError? = null,
) {
    val queens: Set<Cell> get() = gameState?.queens.orEmpty()
    val conflicts: Conflicts get() = gameState?.conflicts ?: Conflicts.Empty
}