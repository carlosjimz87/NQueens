package com.carlosjimz87.rules.solver

import com.carlosjimz87.rules.Constants.MAX_BOARD_SIZE
import com.carlosjimz87.rules.Constants.MIN_BOARD_SIZE
import com.carlosjimz87.rules.model.BoardError
import com.carlosjimz87.rules.model.Cell
import com.carlosjimz87.rules.model.Conflicts
import com.carlosjimz87.rules.model.GameState
import com.carlosjimz87.rules.model.GameStatus
import com.carlosjimz87.rules.either.Result
import com.carlosjimz87.rules.either.Result.Ok
import com.carlosjimz87.rules.either.Result.Err
import com.carlosjimz87.rules.model.ConflictPair

/**
 * This class is a concrete implementation of the [NQueensSolver] interface, it manages the state
 * of an N-Queens puzzle game, by doing the following:
 *
 * - Setting and validating the board size.
 * - Placing and removing queens on the board.
 * - Tracking the number of moves made.
 * - Detecting and reporting conflicts between queens (i.e., queens on the same row, column, or diagonal).
 * - Calculating the current game status (Not Started, In Progress, Solved).
 * - Maintaining the overall game state, including the positions of all queens and any conflicts.
 *
 * This class is stateful, holding the `currentSize`, `currentQueens`, and `movesCount` internally.
 * Each operation that modifies the board (placing or removing a queen) results in a new,
 * immutable [GameState] object that reflects the board's configuration after the move.
 */
class NQueensSolverImpl : NQueensSolver {

    private var currentSize: Int = 0
    private var currentQueens: Set<Cell> = emptySet()
    private var movesCount: Int = 0

    // region - public

    override fun setBoardSize(size: Int): Result<GameState, BoardError> {
        val error = validateSize(size)
        return if (error == BoardError.NoError) {
            currentSize = size
            currentQueens = emptySet()
            movesCount = 0
            Ok(createGameState())
        } else {
            Err(error)
        }
    }

    override fun placeQueen(cell: Cell): GameState {
        movesCount++
        currentQueens = currentQueens + cell
        return createGameState()
    }

    override fun removeQueen(cell: Cell): GameState {
        movesCount++
        currentQueens = currentQueens - cell
        return createGameState()
    }

    // endregion

    // region - private
    private fun validateSize(size: Int): BoardError {
        return when {
            size < MIN_BOARD_SIZE -> BoardError.SizeTooSmall
            size > MAX_BOARD_SIZE -> BoardError.SizeTooBig
            else -> BoardError.NoError
        }
    }

    private fun createGameState(): GameState {
        val conflicts = checkConflicts(currentSize, currentQueens)
        val status = calculateStatus(currentSize, currentQueens.size, conflicts, movesCount)
        return GameState(
            size = currentSize,
            queens = currentQueens,
            conflicts = conflicts,
            status = status
        )
    }

    private fun checkConflicts(size: Int, queens: Set<Cell>): Conflicts {
        if (queens.size < 2) return Conflicts.Empty

        val conflictLines = findConflictLines(queens)
        if (conflictLines.isEmpty()) return Conflicts.Empty

        val conflictsByCell = mutableMapOf<Cell, MutableSet<Cell>>()
        val conflictPairs = mutableSetOf<ConflictPair>()

        for (line in conflictLines) {
            processConflictLine(line, conflictsByCell, conflictPairs)
        }

        return Conflicts(
            conflictsByCell = conflictsByCell.mapValues { it.value.toSet() },
            pairs = conflictPairs.toList()
        )
    }

    private fun findConflictLines(queens: Set<Cell>): List<List<Cell>> {
        val byRow = queens.groupBy { it.row }
        val byCol = queens.groupBy { it.col }
        val byDiag = queens.groupBy { it.row - it.col } // Main diagonal
        val byAnti = queens.groupBy { it.row + it.col } // Anti-diagonal

        return (byRow.values + byCol.values + byDiag.values + byAnti.values)
            .filter { it.size > 1 }
    }

    private fun processConflictLine(
        line: List<Cell>,
        conflictsByCell: MutableMap<Cell, MutableSet<Cell>>,
        conflictPairs: MutableSet<ConflictPair>
    ) {
        for (i in line.indices) {
            for (j in (i + 1) until line.size) {
                val a = line[i]
                val b = line[j]

                // Add the conflict relationship for both queens
                conflictsByCell.getOrPut(a) { mutableSetOf() }.add(b)
                conflictsByCell.getOrPut(b) { mutableSetOf() }.add(a)

                // Add a single, order-independent pair for drawing lines
                conflictPairs.add(createOrderedPair(a, b))
            }
        }
    }

    private fun createOrderedPair(a: Cell, b: Cell): ConflictPair {
        // Ensure consistent ordering to avoid duplicate pairs like (a,b) and (b,a)
        return if (a.hashCode() < b.hashCode()) {
            ConflictPair(a, b)
        } else {
            ConflictPair(b, a)
        }
    }


    private fun calculateStatus(size: Int, queensCount: Int, conflicts: Conflicts, moves: Int): GameStatus {
        return when (queensCount) {
            0 -> GameStatus.NotStarted(size)
            size if !conflicts.hasConflicts -> GameStatus.Solved(
                size = size,
                moves = moves,
            )
            else -> GameStatus.InProgress(
                size = size,
                queensPlaced = queensCount,
                conflicts = conflicts.conflictLinesCount,
            )
        }
    }
    // endregion
}
