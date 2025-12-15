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

    /**
     * Initializes or re-initializes the game board with a specified size.
     * This operation resets all placed queens and the move count.
     *
     * @param size The desired size (N) for the N-Queens board. Must be between [MIN_BOARD_SIZE] and [MAX_BOARD_SIZE].
     * @return A [Result] indicating success ([Ok]) with the initial [GameState] or
     *         failure ([Err]) with a [BoardError] if the size is invalid.
     */
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

    /**
     * Places a queen on the specified cell of the board.
     * This operation increments the move count and returns a new [GameState] reflecting the change.
     *
     * @param cell The [Cell] where the queen is to be placed.
     * @return A new immutable [GameState] object after placing the queen.
     */
    override fun placeQueen(cell: Cell): GameState {
        movesCount++
        currentQueens = currentQueens + cell
        return createGameState()
    }

    /**
     * Removes a queen from the specified cell of the board.
     * This operation increments the move count and returns a new [GameState] reflecting the change.
     *
     * @param cell The [Cell] from where the queen is to be removed.
     * @return A new immutable [GameState] object after removing the queen.
     */
    override fun removeQueen(cell: Cell): GameState {
        movesCount++
        currentQueens = currentQueens - cell
        return createGameState()
    }

    // endregion

    // region - private
    private fun validateSize(size: Int): BoardError {
        /**
         * Validates if the given board size is within acceptable limits.
         *
         * @param size The board size to validate.
         * @return [BoardError.NoError] if the size is valid, otherwise an appropriate [BoardError].
         */
        return when {
            size < MIN_BOARD_SIZE -> BoardError.SizeTooSmall
            size > MAX_BOARD_SIZE -> BoardError.SizeTooBig
            else -> BoardError.NoError
        }
    }

    /**
     * Creates an immutable [GameState] object based on the solver's current internal state.
     * This involves checking for conflicts and calculating the current game status.
     *
     * @return A new [GameState] object reflecting the current board configuration, conflicts, and status.
     */
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

    /**
     * Identifies and returns all conflicts among the currently placed queens on the board.
     * A conflict occurs when two or more queens share the same row, column, or diagonal.
     *
     * @param size The current size of the N-Queens board (though primarily uses `queens` set).
     * @param queens The set of [Cell] objects where queens are currently placed.
     * @return A [Conflicts] object containing details about conflicting cells and pairs,
     *         or [Conflicts.Empty] if no conflicts are found.
     */
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

    /**
     * Groups queens that are in conflict along rows, columns, and diagonals.
     * This function identifies lines (rows, columns, diagonals) that contain more than one queen.
     *
     * @param queens The set of [Cell] objects representing the positions of placed queens.
     * @return A [List] of [List] of [Cell] where each inner list represents a line (row, column, or diagonal)
     *         with two or more queens, indicating a conflict.
     */
    private fun findConflictLines(queens: Set<Cell>): List<List<Cell>> {
        val byRow = queens.groupBy { it.row }
        val byCol = queens.groupBy { it.col }
        val byDiag = queens.groupBy { it.row - it.col } // Main diagonal
        val byAnti = queens.groupBy { it.row + it.col } // Anti-diagonal

        return (byRow.values + byCol.values + byDiag.values + byAnti.values)
            .filter { it.size > 1 }
    }

    /**
     * Processes a single conflict line (e.g., a row, column, or diagonal with multiple queens)
     * to populate the `conflictsByCell` map and `conflictPairs` set.
     *
     * @param line A [List] of [Cell] objects that are all in conflict along a single line.
     * @param conflictsByCell A mutable map to store, for each conflicting queen,
     *                        the set of other queens it is in conflict with.
     * @param conflictPairs A mutable set to store unique, order-independent pairs of conflicting queens,
     *                      used primarily for drawing conflict lines on the UI.
     */
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

    /**
     * Creates an order-independent [ConflictPair] from two cells.
     * This ensures that a pair (A, B) is considered the same as (B, A),
     * preventing duplicate conflict pairs when processing conflict lines.
     * The ordering is based on the hash code of the [Cell] objects.
     *
     * @param a The first [Cell] in the pair.
     * @param b The second [Cell] in the pair.
     * @return A [ConflictPair] with its cells consistently ordered.
     */
    private fun createOrderedPair(a: Cell, b: Cell): ConflictPair {
        // Ensure consistent ordering to avoid duplicate pairs like (a,b) and (b,a)
        return if (a.hashCode() < b.hashCode()) {
            ConflictPair(a, b)
        } else {
            ConflictPair(b, a)
        }
    }


    /**
     * Calculates the current [GameStatus] based on the number of queens placed and detected conflicts.
     *
     * @param size The size of the N-Queens board.
     * @param queensCount The total number of queens currently placed on the board.
     * @param conflicts The [Conflicts] object indicating any conflicts among queens.
     * @param moves The total number of moves made (placements or removals).
     * @return The determined [GameStatus]: [GameStatus.NotStarted], [GameStatus.Solved], or [GameStatus.InProgress].
     */
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
