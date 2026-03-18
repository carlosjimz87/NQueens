package com.carlosjimz87.nqueens.presentation.board.state

import com.carlosjimz87.rules.model.Cell
import com.carlosjimz87.rules.model.ConflictPair
import com.carlosjimz87.rules.model.Conflicts
import com.carlosjimz87.rules.model.GameState
import com.carlosjimz87.rules.model.GameStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BoardStateTest {

    @Test
    fun `queens returns empty set when gameState is null`() {
        val state = BoardState()
        assertTrue(state.queens.isEmpty())
    }

    @Test
    fun `queens returns queens from gameState when not null`() {
        val cell = Cell(row = 2, col = 3)
        val gs = GameState(
            size = 4,
            queens = setOf(cell),
            status = GameStatus.InProgress(size = 4, queensPlaced = 1, conflicts = 0)
        )
        val state = BoardState(gameState = gs, boardSize = 4)
        assertEquals(setOf(cell), state.queens)
    }

    @Test
    fun `conflicts returns Conflicts_Empty when gameState is null`() {
        val state = BoardState()
        assertEquals(Conflicts.Empty, state.conflicts)
        assertFalse(state.conflicts.hasConflicts)
    }

    @Test
    fun `conflicts returns the conflicts stored in gameState`() {
        val a = Cell(0, 0)
        val b = Cell(0, 1)
        val pair = ConflictPair(a, b)
        val conflicts = Conflicts(
            conflictsByCell = mapOf(a to setOf(b), b to setOf(a)),
            pairs = listOf(pair)
        )
        val gs = GameState(
            size = 4,
            queens = setOf(a, b),
            conflicts = conflicts,
            status = GameStatus.InProgress(size = 4, queensPlaced = 2, conflicts = 1)
        )
        val state = BoardState(gameState = gs, boardSize = 4)
        assertTrue(state.conflicts.hasConflicts)
        assertEquals(1, state.conflicts.conflictLinesCount)
        assertTrue(a in state.conflicts.conflictCells)
        assertTrue(b in state.conflicts.conflictCells)
    }

    @Test
    fun `default BoardState has Normal phase, no loading, no error, zero elapsed`() {
        val state = BoardState()
        assertFalse(state.isLoading)
        assertNull(state.boardSize)
        assertNull(state.gameState)
        assertEquals(0L, state.elapsedMillis)
        assertNull(state.latestRank)
        assertEquals(BoardPhase.Normal, state.boardPhase)
        assertNull(state.error)
    }
}
