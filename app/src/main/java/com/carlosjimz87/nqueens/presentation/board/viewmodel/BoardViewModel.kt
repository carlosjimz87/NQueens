package com.carlosjimz87.nqueens.presentation.board.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlosjimz87.nqueens.presentation.board.event.UiEvent
import com.carlosjimz87.nqueens.presentation.board.state.UiState
import com.carlosjimz87.nqueens.presentation.timer.GameTimer
import com.carlosjimz87.nqueens.data.model.LatestRank
import com.carlosjimz87.nqueens.data.repo.StatsRepository
import com.carlosjimz87.rules.either.Result
import com.carlosjimz87.rules.model.Cell
import com.carlosjimz87.rules.model.Conflicts
import com.carlosjimz87.rules.model.GameState
import com.carlosjimz87.rules.model.GameStatus
import com.carlosjimz87.rules.solver.NQueensSolver
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.carlosjimz87.nqueens.data.model.Leaderboards
import com.carlosjimz87.nqueens.presentation.board.model.BoardPhase
import kotlinx.coroutines.flow.Flow

/**
 * ViewModel for the N-Queens game board screen.
 *
 * This class orchestrates the UI logic for the game board.
 * The ViewModel manages the entire lifecycle of a game session, from setting the board size
 * to handling queen placements/removals, tracking time, and recording game statistics upon completion.
 *
 * @param timer The [GameTimer] instance to track the elapsed time of the game.
 * @param solver The [NQueensSolver] instance used to manage the game logic and state.
 * @param statsRepo The [StatsRepository] for recording game stats and retrieving leaderboards.
 * @param initialSize The optional initial size of the board to be set up on creation.
 *
 * @property conflicts A [StateFlow] emitting the current set of conflicting cells on the board.
 * @property boardSize A [StateFlow] emitting the current size (N) of the N-Queens board.
 * @property uiState A [StateFlow] representing the current state of the UI (e.g., Idle, Loading, InvalidBoard).
 * @property queens A [StateFlow] emitting the set of cells where queens are currently placed.
 * @property gameStatus A [StateFlow] emitting the current status of the game (e.g., NotStarted, InProgress, Solved).
 * @property gameState A [StateFlow] emitting the complete, unified state of the game from the solver.
 */
class BoardViewModel(
    private val timer: GameTimer,
    private val solver: NQueensSolver,
    private val statsRepo: StatsRepository,
    private val initialSize: Int? = null,
) : ViewModel() {

    init {
        initialSize?.let { size ->
            applySize(size = size, force = true)
        }
    }

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

    private val _gameState = MutableStateFlow<GameState?>(null)
    val gameState: StateFlow<GameState?> = _gameState

    private val _boardPhase = MutableStateFlow(BoardPhase.Normal)
    val boardPhase: StateFlow<BoardPhase> = _boardPhase

    val elapsedMillis: StateFlow<Long> = timer.elapsedMillis

    private val _events = MutableSharedFlow<UiEvent>(replay = 0, extraBufferCapacity = 1)
    val events: SharedFlow<UiEvent> = _events

    private val _latestRank = MutableStateFlow<LatestRank?>(null)
    val latestRank: StateFlow<LatestRank?> = _latestRank

    fun leaderboards(size: Int) = statsRepo.leaderboards(size)

    fun onSizeChanged(newSize: Int) {
        applySize(size = newSize, force = false)
    }

    private fun applySize(size: Int, force: Boolean) {
        if (!force && _boardSize.value == size) return

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            _boardPhase.value = BoardPhase.Normal // UI resets phase when board changes
            _latestRank.value = null              // optional: reset latest rank on size change
            timer.reset()

            when (val result = solver.setBoardSize(size)) {
                is Result.Ok -> {
                    updateState(result.value)
                    _uiState.value = UiState.Idle
                    emit(UiEvent.BoardReset)
                }

                is Result.Err -> {
                    _uiState.value = UiState.InvalidBoard(error = result.error)
                }
            }
        }
    }

    fun onCellClicked(cell: Cell) {
        // TODO: block clicks during animation phase
        if (_boardPhase.value != BoardPhase.Normal) return

        val currentQueens = _queens.value
        val isPlacing = cell !in currentQueens

        if (isPlacing && currentQueens.isEmpty()) timer.start()

        val newState = if (isPlacing) solver.placeQueen(cell) else solver.removeQueen(cell)
        updateState(newState)

        val conflictOnPlacedCell = isPlacing && newState.conflicts.conflictsFor(cell).isNotEmpty()
        emit(moveEvent(isPlacing, cell, conflictOnPlacedCell))

        if (newState.status is GameStatus.Solved) {
            timer.stop()

            val solved = newState.status as GameStatus.Solved
            val time = elapsedMillis.value

            viewModelScope.launch {
                val result = statsRepo.record(
                    size = solved.size,
                    timeMillis = time,
                    moves = solved.moves
                )
                _latestRank.value = LatestRank(result.rankByTime, result.rankByMoves)
            }
        }
    }

    private fun updateState(gs: GameState) {
        _gameState.value = gs
        _boardSize.value = gs.size
        _queens.value = gs.queens
        _conflicts.value = gs.conflicts
        _gameStatus.value = gs.status
    }

    private fun moveEvent(isPlacing: Boolean, cell: Cell, conflictStarted: Boolean): UiEvent =
        when {
            !isPlacing -> UiEvent.QueenRemoved(cell)
            conflictStarted -> UiEvent.ConflictDetected
            else -> UiEvent.QueenPlaced(cell)
        }

    private fun emit(event: UiEvent) {
        _events.tryEmit(event)
    }

    fun resetGame() {
        _latestRank.value = null
        _boardPhase.value = BoardPhase.Normal
        val size = _boardSize.value ?: return
        applySize(size = size, force = true)
    }

    fun enterWinAnimation() {
        _boardPhase.value = BoardPhase.WinAnimating
    }

    fun onWinAnimationFinished() {
        _boardPhase.value = BoardPhase.WinFrozen
    }
}