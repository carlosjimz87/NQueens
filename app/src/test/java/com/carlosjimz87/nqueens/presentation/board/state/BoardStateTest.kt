package com.carlosjimz87.nqueens.presentation.board.state

import com.carlosjimz87.rules.model.Cell
import com.carlosjimz87.rules.model.Conflicts
import com.carlosjimz87.rules.model.ConflictPair
import com.carlosjimz87.rules.model.GameState
import com.carlosjimz87.rules.model.GameStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class BoardStateTest {

    @Test
    fun `default state has null boardSize, false isLoading, Normal phase, and no error`() {
        val state = BoardState()

        assertNull(state.boardSize)
        assertFalse(state.isLoading)
        assertEquals(BoardPhase.Normal, state.boardPhase)
        assertNull(state.error)
    }

    @Test
    fun `queens returns empty set when gameState is null`() {
        val state = BoardState(gameState = null)

        assertEquals(emptySet<Cell>(), state.queens)
    }

    @Test
    fun `queens returns gameState queens when gameState is present`() {
        val queens = setOf(Cell(0, 1), Cell(2, 3))
        val gameState = GameState(
            size = 4,
            queens = queens,
            status = GameStatus.InProgress(size = 4, queensPlaced = 2, conflicts = 0),
        )
        val state = BoardState(gameState = gameState)

        assertEquals(queens, state.queens)
    }

    @Test
    fun `conflicts returns Empty when gameState is null`() {
        val state = BoardState(gameState = null)

        assertEquals(Conflicts.Empty, state.conflicts)
    }

    @Test
    fun `conflicts returns gameState conflicts when gameState is present`() {
        val cellA = Cell(0, 0)
        val cellB = Cell(1, 1)
        val conflicts = Conflicts(
            conflictsByCell = mapOf(cellA to setOf(cellB), cellB to setOf(cellA)),
            pairs = listOf(ConflictPair(cellA, cellB)),
        )
        val gameState = GameState(
            size = 4,
            queens = setOf(cellA, cellB),
            conflicts = conflicts,
            status = GameStatus.InProgress(size = 4, queensPlaced = 2, conflicts = 1),
        )
        val state = BoardState(gameState = gameState)

        assertEquals(conflicts, state.conflicts)
    }

    @Test
    fun `BoardPhase enum has exactly three values`() {
        val values = BoardPhase.entries

        assertEquals(3, values.size)
        assertEquals(
            listOf(BoardPhase.Normal, BoardPhase.WinAnimating, BoardPhase.WinFrozen),
            values,
        )
    }
}
