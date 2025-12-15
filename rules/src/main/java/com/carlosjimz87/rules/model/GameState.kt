package com.carlosjimz87.rules.model

data class GameState(
    val size: Int,
    val queens: Set<Cell> = emptySet(),
    val conflicts: Conflicts = Conflicts.Empty,
    val status: GameStatus,
    val boardPhase : BoardPhase = BoardPhase.Normal
)