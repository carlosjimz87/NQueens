package com.carlosjimz87.nqueens.presentation.board.viewmodel

import androidx.lifecycle.viewModelScope
import com.carlosjimz87.nqueens.data.model.LatestRank
import com.carlosjimz87.nqueens.data.repo.StatsRepository
import com.carlosjimz87.nqueens.presentation.audio.model.Sound
import com.carlosjimz87.nqueens.presentation.board.effect.BoardEffect
import com.carlosjimz87.nqueens.presentation.board.intent.BoardIntent
import com.carlosjimz87.nqueens.presentation.board.state.BoardPhase
import com.carlosjimz87.nqueens.presentation.board.state.BoardState
import com.carlosjimz87.nqueens.presentation.board.viewmodel.base.BaseViewModel
import com.carlosjimz87.nqueens.presentation.timer.GameTimer
import com.carlosjimz87.rules.either.Result
import com.carlosjimz87.rules.model.BoardError
import com.carlosjimz87.rules.model.Cell
import com.carlosjimz87.rules.model.GameState
import com.carlosjimz87.rules.model.GameStatus
import com.carlosjimz87.rules.solver.NQueensSolver
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * ViewModel for the N-Queens game board screen.
 *
 * This class orchestrates the UI logic for the game board.
 * The ViewModel manages the entire lifecycle of a game session, from setting the board size
 * to handling queen placements/removals, tracking time, and recording game statistics upon completion.
 *
 * It follows a MVI (Model-View-Intent) architecture, processing [BoardIntent]s to produce
 * a [BoardState] and sending [BoardEffect]s for one-off events like playing sounds or
 * showing snackbars.
 *
 * @param timer The [GameTimer] instance to manage the game clock.
 * @param solver The [NQueensSolver] instance that contains the core game logic.
 * @param statsRepo The repository for persisting and retrieving game statistics.
 * @param initialSize An optional initial board size to set up when the ViewModel is created.
 */
class BoardViewModel(
    private val timer: GameTimer,
    private val solver: NQueensSolver,
    private val statsRepo: StatsRepository,
    initialSize: Int? = null,
) : BaseViewModel<BoardIntent, BoardState, BoardEffect>(
    initialState = BoardState()
) {

    companion object{
        const val INVALID_BOARD_SIZE_KEY = "invalid_board_size"
    }
    init {
        // Timer -> State
        timer.elapsedMillis
            .onEach { t -> setState { copy(elapsedMillis = t) } }
            .launchIn(viewModelScope)

        // Initial size
        initialSize?.let { dispatch(BoardIntent.SetBoardSize(it)) }
    }

    fun leaderboards(size: Int) = statsRepo.leaderboards(size)

    override suspend fun handle(intent: BoardIntent) {
        when (intent) {
            is BoardIntent.SetBoardSize -> applyBoardSize(intent.size, force = false)
            BoardIntent.ResetGame -> resetGame()

            is BoardIntent.ClickCell -> clickCell(intent.cell)

            BoardIntent.EnterWinAnimation -> setState { copy(boardPhase = BoardPhase.WinAnimating) }
            BoardIntent.WinAnimationFinished -> setState { copy(boardPhase = BoardPhase.WinFrozen) }
        }
    }

    private suspend fun resetGame() {
        val size = state.value.boardSize ?: return
        applyBoardSize(size, force = true)
    }

    private suspend fun applyBoardSize(size: Int, force: Boolean) {
        val current = state.value.boardSize
        if (!force && current == size) return

        // Pre-reset UI + timer
        resetBoard()

        when (val result = solver.setBoardSize(size)) {
            is Result.Ok -> {
                onBoardSizeApplied(result.value)
            }

            is Result.Err -> {
                onBoardSizeRejected(result.error)
            }
        }
    }

    private fun resetBoard() {
        timer.reset()
        setState {
            copy(
                isLoading = true,
                error = null,
                latestRank = null,
                boardPhase = BoardPhase.Normal
            )
        }
    }

    private suspend fun onBoardSizeApplied(gs: GameState) {
        setState {
            copy(
                isLoading = false,
                error = null,
                boardSize = gs.size,
                gameState = gs,
                boardPhase = BoardPhase.Normal
            )
        }
        postEffect(BoardEffect.PlaySound(Sound.BOARD_RESET))
    }

    private suspend fun onBoardSizeRejected(error: BoardError) {
        setState { copy(isLoading = false, error = error) }
        postEffect(BoardEffect.ShowSnackbar(INVALID_BOARD_SIZE_KEY))
    }

    private suspend fun clickCell(cell: Cell) {
        val currentQueens = state.value.queens
        val isPlacing = cell !in currentQueens

        if (isPlacing && currentQueens.isEmpty()) timer.start()

        val newState = if (isPlacing) solver.placeQueen(cell) else solver.removeQueen(cell)

        setState { copy(gameState = newState, error = null) }

        val sound = when {
            !isPlacing -> Sound.QUEEN_REMOVED
            newState.conflicts.conflictsFor(cell).isNotEmpty() -> Sound.CONFLICT_DETECTED
            else -> Sound.QUEEN_PLACED
        }
        postEffect(BoardEffect.PlaySound(sound))

        if (newState.status is GameStatus.Solved) {
            timer.stop()
            postEffect(BoardEffect.PlaySound(Sound.SOLVED))

            val solved = newState.status as GameStatus.Solved
            val time = state.value.elapsedMillis

            val result = statsRepo.record(
                size = solved.size,
                timeMillis = time,
                moves = solved.moves
            )

            setState { copy(latestRank = LatestRank(result.rankByTime, result.rankByMoves)) }

            // UI phase: animate win
            setState { copy(boardPhase = BoardPhase.WinAnimating) }
        }
    }
}