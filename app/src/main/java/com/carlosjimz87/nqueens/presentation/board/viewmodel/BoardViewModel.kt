package com.carlosjimz87.nqueens.presentation.board.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlosjimz87.nqueens.common.Constants
import com.carlosjimz87.nqueens.presentation.board.event.UiEvent
import com.carlosjimz87.nqueens.presentation.board.state.UiState
import com.carlosjimz87.nqueens.presentation.timer.GameTimer
import com.carlosjimz87.rules.either.Result
import com.carlosjimz87.rules.model.Cell
import com.carlosjimz87.rules.model.Conflicts
import com.carlosjimz87.rules.model.GameState
import com.carlosjimz87.rules.model.GameStatus
import com.carlosjimz87.rules.solver.NQueensSolver
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
 * @param solver The [NQueensSolver] instance used to manage the game logic.
 *
 * @property conflicts A [StateFlow] emitting the current set of conflicting cells on the board.
 * @property boardSize A [StateFlow] emitting the current size (N) of the N-Queens board.
 * @property uiState A [StateFlow] representing the current state of the UI (e.g., Idle, Loading, Error).
 * @property queens A [StateFlow] emitting the set of cells where queens are currently placed.
 * @property gameStatus A [StateFlow] emitting the current status of the game (e.g., NotStarted, InProgress, Solved).
 * @property elapsedMillis A [StateFlow] emitting the elapsed game time in milliseconds.
 * @property events A [SharedFlow] for sending one-time UI events (like sounds or toasts) to the view.
 */
class BoardViewModel(
    private val timer: GameTimer,
    private val solver: NQueensSolver,
) : ViewModel() {

    private val _conflicts = MutableStateFlow(Conflicts.Empty)
    val conflicts: StateFlow<Conflicts> = _conflicts

    private val _boardSize = MutableStateFlow<Int?>(null)
    val boardSize: StateFlow<Int?> = _boardSize

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    private val _queens = MutableStateFlow<Set<Cell>>(emptySet())
    val queens: StateFlow<Set<Cell>> = _queens

    private val _gameStatus = MutableStateFlow<GameStatus?>(null)
    val gameStatus: StateFlow<GameStatus?> = _gameStatus

    val elapsedMillis: StateFlow<Long> = timer.elapsedMillis

    private val _events = MutableSharedFlow<UiEvent>(replay = 0, extraBufferCapacity = 1)
    val events: SharedFlow<UiEvent> = _events

    init {
        onSizeChanged(Constants.DEFAULT_COLUMNS_ROWS)
    }

    fun onSizeChanged(newSize: Int) {
        if (_boardSize.value == newSize) return

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            delay(150)

            solver.setBoardSize(newSize)
                .let { result ->
                    when (result) {
                        is Result.Ok -> {
                            timer.reset()
                            updateState(result.value)
                            _uiState.value = UiState.Idle
                            emit(UiEvent.BoardReset)
                        }

                        is Result.Err -> {
                            _uiState.value = UiState.InvalidBoard(
                                message = "Invalid size: $newSize",
                                error = result.error
                            )
                        }

                        else -> {}
                    }
                }

        }
    }

    fun onCellClicked(cell: Cell) {
        val currentQueens = _queens.value
        val isPlacing = cell !in currentQueens

        if (isPlacing && currentQueens.isEmpty()) timer.start()

        val state = if (isPlacing) solver.placeQueen(cell) else solver.removeQueen(cell)
        updateState(state)

        val conflictOnPlacedCell = isPlacing && state.conflicts.conflictsFor(cell).isNotEmpty()
        emit(moveEvent(isPlacing, cell, conflictOnPlacedCell))

        if (state.status is GameStatus.Solved) timer.stop()
    }

    private fun updateState(gameState: GameState) {
        _boardSize.value = gameState.size
        _queens.value = gameState.queens
        _conflicts.value = gameState.conflicts
        _gameStatus.value = gameState.status
    }

    private fun moveEvent(isPlacing: Boolean, cell: Cell, conflictStarted: Boolean): UiEvent {
        return when {
            !isPlacing -> UiEvent.QueenRemoved(cell)
            conflictStarted -> UiEvent.ConflictDetected
            else -> UiEvent.QueenPlaced(cell)
        }
    }

    private fun emit(event: UiEvent) {
        _events.tryEmit(event)
    }

    fun resetGame() {
        val size = _boardSize.value ?: return

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            delay(150)

            when (val result = solver.setBoardSize(size)) {
                is Result.Ok -> {
                    updateState(result.value)
                    _uiState.value = UiState.Idle
                    emit(UiEvent.BoardReset)
                    timer.reset()
                }
                is Result.Err -> {
                    _uiState.value = UiState.InvalidBoard(
                        message = "Invalid size: $size",
                        error = result.error
                    )
                }
            }
        }
    }
}