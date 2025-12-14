package com.carlosjimz87.rules.game

import com.carlosjimz87.rules.model.Conflicts
import com.carlosjimz87.rules.model.GameStatus

interface GameStatusCalculator {
    fun compute(size: Int, queensCount: Int, conflicts: Conflicts, moves: Int): GameStatus
}