package com.carlosjimz87.nqueens.ui.screens.board

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.carlosjimz87.nqueens.common.Constants
import com.carlosjimz87.nqueens.presentation.audio.AndroidSoundEffectPlayer
import com.carlosjimz87.nqueens.presentation.board.event.UiEvent
import com.carlosjimz87.nqueens.presentation.board.state.UiState
import com.carlosjimz87.nqueens.presentation.board.viewmodel.BoardViewModel
import com.carlosjimz87.nqueens.store.model.LatestRank
import com.carlosjimz87.nqueens.store.model.Leaderboards
import com.carlosjimz87.nqueens.ui.composables.board.Board
import com.carlosjimz87.nqueens.ui.composables.board.BoardContainer
import com.carlosjimz87.nqueens.ui.composables.board.InvalidBoard
import com.carlosjimz87.nqueens.ui.composables.dialogs.BoardSizeDialog
import com.carlosjimz87.nqueens.ui.composables.dialogs.StatsDialog
import com.carlosjimz87.nqueens.ui.composables.dialogs.WinDialog
import com.carlosjimz87.nqueens.ui.composables.game.GameHud
import com.carlosjimz87.rules.model.GameStatus
import org.koin.androidx.compose.koinViewModel

@Composable
fun BoardScreen(
    modifier: Modifier = Modifier,
    viewModel: BoardViewModel = koinViewModel()
) {
    // --- State Collection ---
    val boardSize by viewModel.boardSize.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val queens by viewModel.queens.collectAsState()
    val conflicts by viewModel.conflicts.collectAsState()
    val gameStatus by viewModel.gameStatus.collectAsState()
    val elapsedMillis by viewModel.elapsedMillis.collectAsState()
    val latestRank by viewModel.latestRank.collectAsState()

    // --- Local UI State ---
    val snackbarHostState = remember { SnackbarHostState() }
    var showChangeSizeDialog by rememberSaveable { mutableStateOf(false) }
    var showStatsDialog by rememberSaveable { mutableStateOf(false) }
    var showWinDialog by rememberSaveable { mutableStateOf(false) }

    val soundPlayer = rememberSoundPlayer()

    // --- Effects ---
    BoardScreenEffects(
        viewModel = viewModel,
        snackbarHostState = snackbarHostState,
        soundPlayer = soundPlayer,
        onSolved = { showWinDialog = true }
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
        onDismissWinDialog = { showWinDialog = false },

        onOpenStatsDialog = { showStatsDialog = true }

    )

    // --- UI Layout ---
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val isLoading = uiState is UiState.Loading

            boardSize?.let { size ->
                GameHud(
                    size = size,
                    status = gameStatus,
                    elapsedMillis = elapsedMillis,
                    isLoading = isLoading,
                    onChange = { showChangeSizeDialog = true },
                    onReset = viewModel::resetGame,
                    onStats = { showStatsDialog = true },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isLoading -> CircularProgressIndicator()

                    boardSize != null -> BoardContainer(Modifier.fillMaxSize()) {
                        Board(
                            size = boardSize!!,
                            queens = queens,
                            conflicts = conflicts,
                            onCellClick = viewModel::onCellClicked
                        )
                    }

                    else -> InvalidBoard(
                        modifier = Modifier.fillMaxWidth(),
                        typography = MaterialTheme.typography,
                        cs = MaterialTheme.colorScheme
                    )
                }
            }
        }
    }
}

@Composable
private fun BoardScreenEffects(
    viewModel: BoardViewModel,
    snackbarHostState: SnackbarHostState,
    soundPlayer: AndroidSoundEffectPlayer,
    onSolved: () -> Unit
) {
    LaunchedEffect(viewModel, snackbarHostState) {
        viewModel.uiState.collect { state ->
            if (state is UiState.InvalidBoard) snackbarHostState.showSnackbar(state.message)
        }
    }

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
            title = "Choose board size",
            initial = Constants.DEFAULT_COLUMNS_ROWS,
            min = 4,
            max = 20,
            confirmText = "Start",
            dismissEnabled = false,
            onDismiss = { },
            onConfirm = { size -> viewModel.onSizeChanged(size) }
        )
    }

    // Change Size
    if (showChangeSizeDialog) {
        BoardSizeDialog(
            title = "Change board size",
            initial = boardSize ?: Constants.DEFAULT_COLUMNS_ROWS,
            min = 4,
            max = 20,
            confirmText = "Apply",
            dismissEnabled = true,
            onDismiss = onDismissChangeSizeDialog,
            onConfirm = { size ->
                viewModel.onSizeChanged(size)
                onDismissChangeSizeDialog()
            }
        )
    }

    // Stats
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

    // Win
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
