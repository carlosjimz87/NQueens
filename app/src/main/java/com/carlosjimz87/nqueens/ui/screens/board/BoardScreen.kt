package com.carlosjimz87.nqueens.ui.screens.board

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.carlosjimz87.nqueens.R
import com.carlosjimz87.nqueens.common.Constants
import com.carlosjimz87.nqueens.data.model.LatestRank
import com.carlosjimz87.nqueens.data.model.Leaderboards
import com.carlosjimz87.nqueens.presentation.audio.AndroidSoundEffectPlayer
import com.carlosjimz87.nqueens.presentation.audio.model.Sound
import com.carlosjimz87.nqueens.presentation.board.effect.BoardEffect
import com.carlosjimz87.nqueens.presentation.board.intent.BoardIntent
import com.carlosjimz87.nqueens.presentation.board.state.BoardPhase
import com.carlosjimz87.nqueens.presentation.board.viewmodel.BoardViewModel
import com.carlosjimz87.nqueens.ui.composables.audio.rememberSoundPlayer
import com.carlosjimz87.nqueens.ui.composables.board.BoardAdaptative
import com.carlosjimz87.nqueens.ui.composables.board.InvalidBoard
import com.carlosjimz87.nqueens.ui.composables.board.WinAnimation
import com.carlosjimz87.nqueens.ui.composables.dialogs.BoardSizeDialog
import com.carlosjimz87.nqueens.ui.composables.dialogs.StatsDialog
import com.carlosjimz87.nqueens.ui.composables.dialogs.WinDialog
import com.carlosjimz87.rules.model.BoardError
import com.carlosjimz87.rules.model.Cell
import com.carlosjimz87.rules.model.GameStatus
import org.koin.androidx.compose.koinViewModel

@Composable
fun BoardScreen(
    modifier: Modifier = Modifier,
    viewModel: BoardViewModel = koinViewModel()
) {
    val cs = MaterialTheme.colorScheme

    val state by viewModel.state.collectAsState()

    val boardSize = state.boardSize
    val isLoading = state.isLoading
    val queens = state.queens
    val conflicts = state.conflicts
    val gameStatus = state.gameState?.status
    val elapsedMillis = state.elapsedMillis
    val latestRank = state.latestRank
    val phase = state.boardPhase

    // Animation Snapshots
    var solvedQueens by rememberSaveable { mutableStateOf<List<Cell>>(emptyList()) }
    var winAnimToken by rememberSaveable { mutableIntStateOf(0) }

    // --- Local UI State ---
    val snackbarHostState = remember { SnackbarHostState() }
    var showChangeSizeDialog by rememberSaveable { mutableStateOf(false) }
    var showStatsDialog by rememberSaveable { mutableStateOf(false) }

    val animatingWin = phase == BoardPhase.WinAnimating
    val showWinDialog = phase == BoardPhase.WinFrozen
    val showQueens = phase == BoardPhase.Normal
    val allowClicks = phase == BoardPhase.Normal

    val soundPlayer = rememberSoundPlayer()

    // --- Effects (sounds/snackbar) ---
    BoardScreenEffects(
        viewModel = viewModel,
        snackbarHostState = snackbarHostState,
        soundPlayer = soundPlayer,
        currentBoardError = state.error
    )

    // --- Win Animation Trigger ---
    LaunchedEffect(phase) {
        if (phase == BoardPhase.WinAnimating) {
            solvedQueens = queens.toList()
            winAnimToken++
        }
    }

    // --- Dialogs ---
    BoardScreenDialogs(
        viewModel = viewModel,
        boardSize = boardSize,
        gameStatus = gameStatus,
        elapsedMillis = elapsedMillis,
        latestRank = latestRank,
        showSetupDialog = boardSize == null,
        showChangeSizeDialog = showChangeSizeDialog,
        showStatsDialog = showStatsDialog,
        showWinDialog = showWinDialog,
        onDismissChangeSizeDialog = { showChangeSizeDialog = false },
        onDismissStatsDialog = { showStatsDialog = false },
        onDismissWinDialog = { /* boardPhase controlled */ },
        onOpenStatsDialog = { showStatsDialog = true }
    )

    // --- UI Layout ---
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->

        when {
            isLoading -> Box(
                Modifier
                    .fillMaxSize()
                    .background(cs.surface)
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            boardSize != null -> BoardAdaptative(
                boardSize = boardSize,
                isLoading = state.isLoading,
                queens = queens,
                conflicts = conflicts,
                gameStatus = gameStatus,
                elapsedMillis = elapsedMillis,
                allowClicks = allowClicks,
                showQueens = showQueens,
                onCellClick = { cell -> if (allowClicks) viewModel.dispatch(BoardIntent.ClickCell(cell)) },
                onChange = { showChangeSizeDialog = true },
                onReset = { viewModel.dispatch(BoardIntent.ResetGame) },
                onStats = { showStatsDialog = true },
                modifier = Modifier
                    .fillMaxSize()
                    .background(cs.surface)
                    .padding(innerPadding),
                winOverlay = {
                    WinAnimation(
                        token = winAnimToken,
                        size = boardSize,
                        queens = solvedQueens,
                        enabled = animatingWin,
                        onFinished = { viewModel.dispatch(BoardIntent.WinAnimationFinished) }
                    )
                }
            )

            else -> Box(
                Modifier
                    .fillMaxSize()
                    .background(cs.surface)
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                InvalidBoard(
                    modifier = Modifier.fillMaxWidth(),
                    typography = MaterialTheme.typography,
                    cs = cs
                )
            }
        }
    }
}

@Composable
private fun BoardScreenEffects(
    viewModel: BoardViewModel,
    snackbarHostState: SnackbarHostState,
    soundPlayer: AndroidSoundEffectPlayer,
    currentBoardError: BoardError?
) {
    val msgTooSmall = stringResource(R.string.board_too_small)
    val msgTooBig = stringResource(R.string.board_too_big)
    val msgInvalid = stringResource(R.string.invalid_size)

    LaunchedEffect(viewModel, snackbarHostState, soundPlayer) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is BoardEffect.PlaySound -> when (effect.sound) {
                    Sound.QUEEN_PLACED -> soundPlayer.playQueenPlaced()
                    Sound.QUEEN_REMOVED -> soundPlayer.playQueenRemoved()
                    Sound.BOARD_RESET -> soundPlayer.playStart()
                    Sound.CONFLICT_DETECTED -> soundPlayer.playConflict()
                    Sound.SOLVED -> soundPlayer.playSolved()
                }

                is BoardEffect.ShowSnackbar -> {
                    val msg = if (effect.message == BoardViewModel.INVALID_BOARD_SIZE_KEY) {
                        when (currentBoardError) {
                            BoardError.SizeTooSmall -> msgTooSmall
                            BoardError.SizeTooBig -> msgTooBig
                            else -> msgInvalid
                        }
                    } else {
                        effect.message
                    }
                    snackbarHostState.showSnackbar(msg)
                }
            }
        }
    }
}

@Composable
private fun BoardScreenDialogs(
    viewModel: BoardViewModel,
    boardSize: Int?,
    gameStatus: GameStatus?,
    elapsedMillis: Long,
    latestRank: LatestRank?,
    showSetupDialog: Boolean,
    showChangeSizeDialog: Boolean,
    showStatsDialog: Boolean,
    showWinDialog: Boolean,
    onDismissChangeSizeDialog: () -> Unit,
    onDismissStatsDialog: () -> Unit,
    onDismissWinDialog: () -> Unit,
    onOpenStatsDialog: () -> Unit
) {
    if (showSetupDialog) {
        BoardSizeDialog(
            title = stringResource(R.string.choose_board_size),
            initial = Constants.DEFAULT_COLUMNS_ROWS,
            min = 4,
            max = 20,
            confirmText = stringResource(id = R.string.start),
            dismissEnabled = false,
            onDismiss = { },
            onConfirm = { size ->
                viewModel.dispatch(BoardIntent.SetBoardSize(size))
            }
        )
    }

    if (showChangeSizeDialog) {
        BoardSizeDialog(
            title = stringResource(R.string.change_board_size),
            initial = boardSize ?: Constants.DEFAULT_COLUMNS_ROWS,
            min = 4,
            max = 20,
            confirmText = stringResource(id = R.string.apply),
            dismissEnabled = true,
            onDismiss = onDismissChangeSizeDialog,
            onConfirm = { size ->
                viewModel.dispatch(BoardIntent.SetBoardSize(size))
                onDismissChangeSizeDialog()
            }
        )
    }

    if (showStatsDialog && boardSize != null) {
        val lb by viewModel.leaderboards(boardSize).collectAsState(
            initial = Leaderboards(emptyList(), emptyList())
        )
        StatsDialog(
            size = boardSize,
            leaderboards = lb,
            onClose = onDismissStatsDialog
        )
    }

    val solved = gameStatus as? GameStatus.Solved
    if (showWinDialog && solved != null) {
        WinDialog(
            solved = solved,
            elapsedMillis = elapsedMillis,
            rankByTime = latestRank?.rankByTime ?: 0,
            rankByMoves = latestRank?.rankByMoves ?: 0,
            onPlayAgain = {
                onDismissWinDialog()
                viewModel.dispatch(BoardIntent.ResetGame)
            },
            onNextLevel = {
                onDismissWinDialog()
                viewModel.dispatch(BoardIntent.SetBoardSize(solved.size + 1))
            },
            onShowStats = {
                onDismissWinDialog()
                onOpenStatsDialog()
            }
        )
    }
}