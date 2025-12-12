package com.carlosjimz87.nqueens.presentation.rules

import com.carlosjimz87.rules.QueenConflictsChecker
import com.carlosjimz87.rules.model.Cell
import com.carlosjimz87.rules.model.Conflicts

/**
 * A fake implementation of [QueenConflictsChecker] for testing purposes.
 */
class FakeQueenConflictsChecker : QueenConflictsChecker{
    override fun check(
        size: Int,
        queens: Set<Cell>
    ): Conflicts {
        return Conflicts.Empty
    }
}