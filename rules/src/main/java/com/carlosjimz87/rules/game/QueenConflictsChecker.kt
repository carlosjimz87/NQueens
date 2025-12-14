package com.carlosjimz87.rules.game

import com.carlosjimz87.rules.model.Cell
import com.carlosjimz87.rules.model.Conflicts

interface QueenConflictsChecker {
    fun check(size: Int, queens: Set<Cell>): Conflicts
}