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
import androidx.compose.ui.platform.LocalContext
import com.carlosjimz87.nqueens.common.Constants
import com.carlosjimz87.nqueens.presentation.audio.AndroidSoundEffectPlayer
import com.carlosjimz87.nqueens.presentation.board.event.UiEvent
import com.carlosjimz87.nqueens.presentation.board.state.UiState
import com.carlosjimz87.nqueens.presentation.board.viewmodel.BoardViewModel
import com.carlosjimz87.nqueens.data.model.LatestRank
import com.carlosjimz87.nqueens.data.model.Leaderboards
import com.carlosjimz87.nqueens.ui.composables.board.BoardAdaptative
import com.carlosjimz87.nqueens.ui.composables.board.InvalidBoard
import com.carlosjimz87.nqueens.ui.composables.board.WinAnimation
import com.carlosjimz87.nqueens.ui.composables.dialogs.BoardSizeDialog
import com.carlosjimz87.nqueens.ui.composables.dialogs.StatsDialog
import com.carlosjimz87.nqueens.ui.composables.dialogs.WinDialog
import com.carlosjimz87.rules.model.Cell
import com.carlosjimz87.rules.model.GameStatus
import androidx.compose.ui.res.stringResource
import com.carlosjimz87.nqueens.R
import com.carlosjimz87.nqueens.presentation.board.model.BoardPhase
import com.carlosjimz87.rules.model.BoardError
import org.koin.androidx.compose.koinViewModel

@Composable
fun BoardScreen(
    modifier: Modifier = Modifier,
    viewModel: BoardViewModel = koinViewModel()
) {
    val cs = MaterialTheme.colorScheme

    // --- State Collection ---
    val boardSize by viewModel.boardSize.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val queens by viewModel.queens.collectAsState()
    val conflicts by viewModel.conflicts.collectAsState()
    val gameStatus by viewModel.gameStatus.collectAsState()
    val elapsedMillis by viewModel.elapsedMillis.collectAsState()
    val latestRank by viewModel.latestRank.collectAsState()
    val phase by viewModel.boardPhase.collectAsState()

    var solvedQueens by rememberSaveable { mutableStateOf<List<Cell>>(emptyList()) }

    // --- Local UI State ---
    val snackbarHostState = remember { SnackbarHostState() }
    var showChangeSizeDialog by rememberSaveable { mutableStateOf(false) }
    var showStatsDialog by rememberSaveable { mutableStateOf(false) }
    var winAnimToken by rememberSaveable { mutableIntStateOf(0) }

    val animatingWin = phase == BoardPhase.WinAnimating
    val showWinDialog = phase == BoardPhase.WinFrozen
    val showQueens = phase == BoardPhase.Normal
    val allowClicks = phase == BoardPhase.Normal

    val soundPlayer = rememberSoundPlayer()

    // --- Effects ---
    BoardScreenEffects(
        viewModel = viewModel,
        snackbarHostState = snackbarHostState,
        soundPlayer = soundPlayer,
        queens = queens,
        onSolved = {
            solvedQueens = queens.toList()
            winAnimToken++
            viewModel.enterWinAnimation()
        }
    )

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
        onDismissWinDialog = { /* lo controla boardPhase */ },
        onOpenStatsDialog = { showStatsDialog = true }
    )

    // --- UI Layout ---
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->

        val isLoading = uiState is UiState.Loading

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
                boardSize = boardSize!!,
                uiState = uiState,
                queens = queens,
                conflicts = conflicts,
                gameStatus = gameStatus,
                elapsedMillis = elapsedMillis,
                allowClicks = allowClicks,
                showQueens = showQueens,
                onCellClick = { cell -> if (allowClicks) viewModel.onCellClicked(cell) },
                onChange = { showChangeSizeDialog = true },
                onReset = viewModel::resetGame,
                onStats = { showStatsDialog = true },
                modifier = Modifier
                    .fillMaxSize()
                    .background(cs.surface)
                    .padding(innerPadding),
                winOverlay = {
                    WinAnimation(
                        token = winAnimToken,
                        size = boardSize!!,
                        queens = solvedQueens,
                        enabled = animatingWin,
                        onFinished = { viewModel.onWinAnimationFinished() }
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
    queens: Set<Cell>,
    onSolved: () -> Unit
) {
    val msgTooSmall = stringResource(R.string.board_too_small)
    val msgTooBig = stringResource(R.string.board_too_big)

    // ✅ invalid board size messages
    LaunchedEffect(viewModel, snackbarHostState) {
        viewModel.uiState.collect { state ->
            if (state is UiState.InvalidBoard) {
                val msg = when (state.error) {
                    BoardError.SizeTooSmall -> msgTooSmall
                    BoardError.SizeTooBig -> msgTooBig
                    else -> null
                }
                if (msg != null) snackbarHostState.showSnackbar(msg)
            }
        }
    }

    // ✅ sounds (one-shot events)
    LaunchedEffect(viewModel, soundPlayer) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.QueenPlaced -> soundPlayer.playQueenPlaced()
                is UiEvent.QueenRemoved -> soundPlayer.playQueenRemoved()
                is UiEvent.BoardReset -> soundPlayer.playStart()
                is UiEvent.ConflictDetected -> soundPlayer.playConflict()
            }
        }
    }

    // ✅ solved detection: only when status changes to Solved
    LaunchedEffect(viewModel, soundPlayer) {
        var prevSolved = false
        viewModel.gameStatus.collect { status ->
            val solvedNow = status is GameStatus.Solved
            if (solvedNow && !prevSolved) {
                soundPlayer.playSolved()
                onSolved()
            }
            prevSolved = solvedNow
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
                viewModel.onSizeChanged(size)
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
                viewModel.onSizeChanged(size)
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
                viewModel.resetGame()
            },
            onNextLevel = {
                onDismissWinDialog()
                viewModel.onSizeChanged(solved.size + 1)
            },
            onShowStats = {
                onDismissWinDialog()
                onOpenStatsDialog()
            }
        )
    }
}

@Composable
private fun rememberSoundPlayer(): AndroidSoundEffectPlayer {
    val context = LocalContext.current
    return remember(context.applicationContext) {
        AndroidSoundEffectPlayer(context.applicationContext)
    }
}