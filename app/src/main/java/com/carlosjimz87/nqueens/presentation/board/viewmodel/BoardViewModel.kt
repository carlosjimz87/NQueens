package com.carlosjimz87.nqueens.presentation.board.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlosjimz87.nqueens.presentation.board.event.UiEvent
import com.carlosjimz87.nqueens.presentation.board.state.UiState
import com.carlosjimz87.nqueens.presentation.timer.GameTimer
import com.carlosjimz87.nqueens.store.model.LatestRank
import com.carlosjimz87.nqueens.store.repo.StatsRepository
import com.carlosjimz87.rules.either.Result
import com.carlosjimz87.rules.model.BoardPhase
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
import com.carlosjimz87.nqueens.store.model.Leaderboards
import kotlinx.coroutines.flow.Flow

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
    private val statsRepo: StatsRepository,
    private val initialSize: Int? = null,
) : ViewModel() {

    /**
     * Initializes the ViewModel. If an [initialSize] is provided, it attempts to apply
     * that board size to start the game.
     */
    init {
        initialSize?.let { applySize(it, force = true) }
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

    val elapsedMillis: StateFlow<Long> = timer.elapsedMillis

    private val _events = MutableSharedFlow<UiEvent>(replay = 0, extraBufferCapacity = 1)
    val events: SharedFlow<UiEvent> = _events

    private val _latestRank = MutableStateFlow<LatestRank?>(null)
    val latestRank: StateFlow<LatestRank?> = _latestRank

    /**
     * Retrieves the leaderboards for a given board size from the [StatsRepository].
     *
     * @param size The board size for which to fetch leaderboards.
     * @return A [Flow] of [Leaderboards] for the specified size.
     */
    fun leaderboards(size: Int) = statsRepo.leaderboards(size)

    /**
     * Handles a request from the UI to change the board size.
     * Delegates to [applySize] to perform the actual size change and game reset.
     *
     * @param newSize The new desired size for the N-Queens board.
     */
    fun onSizeChanged(newSize: Int) = applySize(newSize, force = false)

    /**
     * Applies a new board size, resetting the game state.
     *
     * @param size The new size for the N-Queens board.
     * @param force If true, the size will be applied even if it's the same as the current size.
     *              Useful for re-initializing the board.
     */
    private fun applySize(size: Int, force: Boolean) {
        if (!force && _boardSize.value == size) return

        viewModelScope.launch {
            _uiState.value = UiState.Loading

            timer.reset()

            when (val result = solver.setBoardSize(size)) {
                is Result.Ok -> {
                    val gs = result.value.copy(boardPhase = BoardPhase.Normal)
                    updateState(gs)
                    _uiState.value = UiState.Idle
                    emit(UiEvent.BoardReset)
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

    /**
     * Handles a click event on a specific cell of the N-Queens board.
     * This function determines whether to place a queen or remove an existing one.
     * It also manages the game timer, updates the game state, and records game statistics
     * if the puzzle is solved.
     *
     * @param cell The [Cell] that was clicked by the user.
     */
    fun onCellClicked(cell: Cell) {
        val currentQueens = _queens.value
        val isPlacing = cell !in currentQueens

        if (isPlacing && currentQueens.isEmpty()) timer.start()

        val state = if (isPlacing) solver.placeQueen(cell) else solver.removeQueen(cell)
        updateState(state)

        val conflictOnPlacedCell = isPlacing && state.conflicts.conflictsFor(cell).isNotEmpty()
        emit(moveEvent(isPlacing, cell, conflictOnPlacedCell))

        if (state.status is GameStatus.Solved) {
            timer.stop()

            val solved = state.status as GameStatus.Solved
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

    /**
     * Updates all relevant [MutableStateFlow]s with data from a new [GameState].
     * This method acts as a central point for state updates to ensure consistency.
     *
     * @param gs The new [GameState] to reflect in the UI state.
     */
    private fun updateState(gs: GameState) {
        _gameState.value = gs
        _boardSize.value = gs.size
        _queens.value = gs.queens
        _conflicts.value = gs.conflicts
        _gameStatus.value = gs.status
    }

    /**
     * Creates a [UiEvent] based on the queen movement action and whether a conflict started.
     * This event is typically used to trigger sound effects or visual feedback in the UI.
     *
     * @param isPlacing `true` if a queen was placed, `false` if it was removed.
     * @param cell The [Cell] where the action occurred.
     * @param conflictStarted `true` if placing a queen caused a new conflict, `false` otherwise.
     * @return The appropriate [UiEvent] ([UiEvent.QueenRemoved], [UiEvent.ConflictDetected], or [UiEvent.QueenPlaced]).
     */
    private fun moveEvent(isPlacing: Boolean, cell: Cell, conflictStarted: Boolean): UiEvent {
        return when {
            !isPlacing -> UiEvent.QueenRemoved(cell)
            conflictStarted -> UiEvent.ConflictDetected
            else -> UiEvent.QueenPlaced(cell)
        }
    }

    /**
     * Helper function to emit [UiEvent]s through the [_events] [MutableSharedFlow].
     * This is used for one-time events that the UI should react to (e.g., sound effects).
     *
     * @param event The [UiEvent] to emit.
     */
    private fun emit(event: UiEvent) {
        _events.tryEmit(event)
    }

    /**
     * Resets the current game. This clears any previously recorded ranks and
     * re-initializes the board with the current board size, effectively starting a new game.
     */
    fun resetGame() {
        _latestRank.value = null
        val size = _boardSize.value ?: return
        applySize(size, force = true)
    }

    /**
     * Updates the game state to set the board phase to [BoardPhase.WinAnimating].
     * This signals the UI to start any winning animation sequence.
     */
    fun enterWinAnimation() {
        _gameState.update { it?.copy(boardPhase = BoardPhase.WinAnimating) }
    }

    /**
     * Updates the game state to set the board phase to [BoardPhase.WinFrozen] after the win animation has finished.
     * This typically means the winning state is displayed statically to the user.
     */
    fun onWinAnimationFinished() {
        _gameState.update { it?.copy(boardPhase = BoardPhase.WinFrozen) }
    }
}