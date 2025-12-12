package com.carlosjimz87.rules

import com.carlosjimz87.rules.model.Cell
import com.carlosjimz87.rules.model.Conflicts

interface QueenConflictsChecker {
    fun conflicts(size: Int, queens: Set<Cell>): Conflicts
}