package com.carlosjimz87.rules.game

import com.carlosjimz87.rules.model.Conflicts
import com.carlosjimz87.rules.model.GameStatus

class GameStatusCalculatorImpl : GameStatusCalculator {
    override fun compute(size: Int, queensCount: Int, conflicts: Conflicts, moves: Int): GameStatus {
        val conflictsCount = conflicts.conflictLinesCount

        return when (queensCount) {
            0 -> GameStatus.NotStarted(size)
            size if !conflicts.hasConflicts -> GameStatus.Solved(size = size, moves = moves)
            else -> GameStatus.InProgress(size = size, queensPlaced = queensCount, conflicts = conflictsCount)
        }
    }
}