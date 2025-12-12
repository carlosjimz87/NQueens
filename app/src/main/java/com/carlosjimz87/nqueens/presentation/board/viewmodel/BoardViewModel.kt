package com.carlosjimz87.nqueens.presentation.board.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlosjimz87.nqueens.common.Constants
import com.carlosjimz87.nqueens.common.validateNQueensBoardSize
import com.carlosjimz87.nqueens.domain.error.BoardError
import com.carlosjimz87.rules.model.Cell
import com.carlosjimz87.nqueens.domain.model.GameStatus
import com.carlosjimz87.nqueens.presentation.board.event.UiEvent
import com.carlosjimz87.nqueens.presentation.board.state.UiState
import com.carlosjimz87.nqueens.presentation.timer.GameTimer
import com.carlosjimz87.rules.QueenConflictsChecker
import com.carlosjimz87.rules.model.Conflicts
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the N-Queens game board screen.
 *
 * This class is responsible for holding and managing the UI-related data for the game board,
 * handling user interactions, and communicating with the business logic layer. It exposes
 * various state flows to the UI to observe changes in the game state, such as the board size,
 * queen positions, conflicts, and overall game status.
 *
 * @param timer The [GameTimer] instance to track the elapsed time of the game.
 * @param conflictsChecker The [QueenConflictsChecker] instance used to detect conflicts between queens.
 *
 * @property conflicts A [StateFlow] emitting the current set of conflicting cells on the board.
 * @property boardSize A [StateFlow] emitting the current size (N) of the N-Queens board.
 * @property uiState A [StateFlow] representing the current state of the UI (e.g., Idle, Loading, Error).
 * @property queens A [StateFlow] emitting the set of cells where queens are currently placed.
 * @property events A [SharedFlow] for sending one-time UI events (like sounds or toasts) to the view.
 * @property gameStatus A [StateFlow] emitting the current status of the game (e.g., NotStarted, InProgress, Solved).
 * @property elapsedMillis A [StateFlow] emitting the elapsed game time in milliseconds.
 */
class BoardViewModel(
    private val timer: GameTimer,
    private val conflictsChecker: QueenConflictsChecker
) : ViewModel() {

    private val _conflicts = MutableStateFlow(Conflicts.Empty)
    val conflicts: StateFlow<Conflicts> = _conflicts

    private val _boardSize = MutableStateFlow<Int?>(null)
    val boardSize: StateFlow<Int?> = _boardSize

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    private val _queens = MutableStateFlow<Set<Cell>>(emptySet())
    val queens: StateFlow<Set<Cell>> = _queens

    private val _events = MutableSharedFlow<UiEvent>(replay = 0, extraBufferCapacity = 1)
    val events: SharedFlow<UiEvent> = _events

    private val _gameStatus = MutableStateFlow<GameStatus?>(null)
    val gameStatus: StateFlow<GameStatus?> = _gameStatus

    val elapsedMillis: StateFlow<Long> = timer.elapsedMillis

    private var movesCount: Int = 0
    private var lastConflictCellsCount: Int = 0

    init {
        applySize(Constants.DEFAULT_COLUMNS_ROWS)
    }

    fun onSizeChanged(newSize: Int) = applySize(newSize)

    fun onCellClicked(cell: Cell) {
        val size = _boardSize.value ?: return

        val current = _queens.value
        val isPlacing = cell !in current

        if (isPlacing && current.isEmpty()) timer.start()
        movesCount++

        val nextQueens = if (isPlacing) current + cell else current - cell
        applyQueensSnapshot(size = size, queens = nextQueens, placedCell = if (isPlacing) cell else null)

        emit(if (isPlacing) UiEvent.QueenPlaced(cell) else UiEvent.QueenRemoved(cell))
    }

    private fun applyQueensSnapshot(size: Int, queens: Set<Cell>, placedCell: Cell?) {
        _queens.value = queens

        val nextConflicts = conflictsChecker.check(size = size, queens = queens)
        _conflicts.value = nextConflicts

        maybeEmitConflictDetected(placedCell = placedCell, conflicts = nextConflicts)

        _gameStatus.value = computeStatus(size = size, queens = queens, conflicts = nextConflicts)
    }

    private fun maybeEmitConflictDetected(placedCell: Cell?, conflicts: Conflicts) {
        if (placedCell == null) return

        val newCount = conflicts.conflictCells.size
        if (newCount > lastConflictCellsCount) {
            emit(UiEvent.ConflictDetected)
        }
        lastConflictCellsCount = newCount
    }

    private fun computeStatus(size: Int, queens: Set<Cell>, conflicts: Conflicts): GameStatus {
        val queensCount = queens.size
        val conflictsCount = conflicts.conflictLinesCount

        return when {
            queensCount == 0 -> GameStatus.NotStarted(size)

            queensCount == size && !conflicts.hasConflicts -> {
                timer.stop()
                GameStatus.Solved(size = size, moves = movesCount)
            }

            else -> GameStatus.InProgress(
                size = size,
                queensPlaced = queensCount,
                conflicts = conflictsCount
            )
        }
    }

    private fun applySize(size: Int) {
        if (_boardSize.value == size) return

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            delay(150)

            when (validateNQueensBoardSize(size)) {
                BoardError.NoError -> resetGame(size)
                else -> _uiState.value = UiState.InvalidBoard(
                    message = "Invalid size: $size",
                    error = validateNQueensBoardSize(size)
                )
            }
        }
    }

    private fun resetGame(size: Int) {
        _boardSize.value = size
        movesCount = 0
        lastConflictCellsCount = 0

        timer.reset()
        applyQueensSnapshot(size = size, queens = emptySet(), placedCell = null)

        _uiState.value = UiState.Idle
        emit(UiEvent.BoardReset)
    }

    private fun emit(event: UiEvent) {
        _events.tryEmit(event)
    }
}