package com.carlosjimz87.nqueens.presentation.board.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlosjimz87.nqueens.common.Constants
import com.carlosjimz87.nqueens.presentation.board.event.UiEvent
import com.carlosjimz87.nqueens.presentation.board.state.UiState
import com.carlosjimz87.nqueens.presentation.timer.GameTimer
import com.carlosjimz87.rules.board.BoardChecker
import com.carlosjimz87.rules.game.GameStatusCalculator
import com.carlosjimz87.rules.game.QueenConflictsChecker
import com.carlosjimz87.rules.model.BoardError
import com.carlosjimz87.rules.model.Cell
import com.carlosjimz87.rules.model.Conflicts
import com.carlosjimz87.rules.model.GameStatus
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
    private val conflictsChecker: QueenConflictsChecker,
    private val boardChecker: BoardChecker,
    private val statusCalculator: GameStatusCalculator
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

        val conflictStarted = applyQueensSnapshot(
            size = size,
            queens = nextQueens,
            placedCell = if (isPlacing) cell else null
        )

        val event = when {
            !isPlacing -> UiEvent.QueenRemoved(cell)
            conflictStarted -> UiEvent.ConflictDetected
            else -> UiEvent.QueenPlaced(cell)
        }

        emit(event)
    }

    private fun applyQueensSnapshot(size: Int, queens: Set<Cell>, placedCell: Cell?): Boolean {
        _queens.value = queens

        val nextConflicts = conflictsChecker.check(size = size, queens = queens)
        _conflicts.value = nextConflicts

        val conflictStarted = didConflictStart(placedCell = placedCell, conflicts = nextConflicts)

        val status = statusCalculator.compute(
            size = size,
            queensCount = queens.size,
            conflicts = nextConflicts,
            moves = movesCount
        )

        if (status is GameStatus.Solved) timer.stop()
        _gameStatus.value = status

        return conflictStarted
    }

    private fun didConflictStart(placedCell: Cell?, conflicts: Conflicts): Boolean {
        if (placedCell == null) return false

        val newCount = conflicts.conflictCells.size
        val started = newCount > lastConflictCellsCount
        lastConflictCellsCount = newCount
        return started
    }

    private fun applySize(size: Int) {
        if (_boardSize.value == size) return

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            delay(150)
            when (val sizeError = boardChecker.validateSize(size)) {
                BoardError.NoError -> resetGame(size)
                else -> _uiState.value = UiState.InvalidBoard(
                    message = "Invalid size: $size",
                    error = sizeError
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