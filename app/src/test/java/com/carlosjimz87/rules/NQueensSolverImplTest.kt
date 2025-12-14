package com.carlosjimz87.rules

import com.carlosjimz87.rules.model.BoardError
import com.carlosjimz87.rules.model.Cell
import com.carlosjimz87.rules.model.GameStatus
import com.carlosjimz87.rules.solver.NQueensSolverImpl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NQueensSolverImplTest {

    private lateinit var solver: NQueensSolverImpl

    @Before
    fun setup() {
        solver = NQueensSolverImpl()
    }

    @Test
    fun `setBoardSize with valid size updates game state correctly`() {
        val size = 4
        val result = solver.setBoardSize(size)
        val gameState = result.getOrNull()

        assertNotNull(gameState)
        assertEquals(size, gameState!!.size)
        assertTrue(gameState.conflicts.hasConflicts == false)

        assertTrue(gameState!!.status is GameStatus.NotStarted)
        assertEquals(size, (gameState!!.status as? GameStatus.NotStarted)?.size)
    }

    @Test
    fun `setBoardSize with size less than 4 returns SizeTooSmall error`() {
        val size = 3
        val result = solver.setBoardSize(size)
        val error = result.getErrorOrNull()

        assertNotNull(error)
        assertEquals(BoardError.SizeTooSmall, error)
    }

    @Test
    fun `setBoardSize with size greater than 20 returns SizeTooBig error`() {
        val size = 21
        val result = solver.setBoardSize(size)
        val error = result.getErrorOrNull()

        assertNotNull(error)
        assertEquals(BoardError.SizeTooBig, error)
    }

    @Test
    fun `placeQueen adds queen and updates state`() {
        solver.setBoardSize(4)
        val cell = Cell(0, 0)
        val gameState = solver.placeQueen(cell)

        assertEquals(1, gameState.queens.size)
        assertTrue(cell in gameState.queens)
        assertTrue(gameState.status is GameStatus.InProgress)
    }

    @Test
    fun `removeQueen removes queen and updates state`() {
        solver.setBoardSize(4)
        val cell = Cell(0, 0)
        solver.placeQueen(cell)
        val gameState = solver.removeQueen(cell)

        assertTrue(gameState.conflicts.hasConflicts == false)
        assertFalse(cell in gameState.queens)
        assertTrue(gameState.status is GameStatus.NotStarted)
    }

    @Test
    fun `placing conflicting queens updates conflicts`() {
        solver.setBoardSize(4)
        solver.placeQueen(Cell(0, 0))
        val gameState = solver.placeQueen(Cell(1, 1)) // Diagonal conflict

        assertTrue(gameState.conflicts.hasConflicts)
        assertTrue(Cell(0, 0) in gameState.conflicts.conflictCells)
        assertTrue(Cell(1, 1) in gameState.conflicts.conflictCells)
    }

    @Test
    fun `solving the puzzle updates game status to Solved`() {
        solver.setBoardSize(4)
        solver.placeQueen(Cell(0, 1))
        solver.placeQueen(Cell(1, 3))
        solver.placeQueen(Cell(2, 0))
        val gameState = solver.placeQueen(Cell(3, 2)) // Solved state for 4x4

        assertTrue(gameState.status is GameStatus.Solved)
        assertEquals(4, (gameState.status as GameStatus.Solved).size)
    }

    @Test
    fun `removing last queen on solved board returns to NotStarted`() {
        solver.setBoardSize(4)
        solver.placeQueen(Cell(0, 1))
        solver.placeQueen(Cell(1, 3))
        solver.placeQueen(Cell(2, 0))
        solver.placeQueen(Cell(3, 2)) // Solved state

        val gameState = solver.removeQueen(Cell(3, 2))

        assertTrue(gameState.status is GameStatus.InProgress)
    }
}
