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
import androidx.compose.runtime.mutableIntStateOf
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
import com.carlosjimz87.nqueens.ui.composables.board.Board
import com.carlosjimz87.nqueens.ui.composables.board.BoardContainer
import com.carlosjimz87.nqueens.ui.composables.board.InvalidBoard
import com.carlosjimz87.nqueens.ui.composables.dialogs.BoardSizeDialog
import com.carlosjimz87.nqueens.ui.composables.dialogs.WinDialog
import com.carlosjimz87.nqueens.ui.composables.game.GameHud
import com.carlosjimz87.rules.model.GameStatus
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun BoardScreen(
    modifier: Modifier = Modifier,
    viewModel: BoardViewModel = koinViewModel()
) {
    val cs = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    val boardSize by viewModel.boardSize.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val queens by viewModel.queens.collectAsState()
    val conflicts by viewModel.conflicts.collectAsState()
    val gameStatus by viewModel.gameStatus.collectAsState()
    val elapsedMillis by viewModel.elapsedMillis.collectAsState()
    var didCompleteSetup by rememberSaveable { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val soundPlayer = remember(context.applicationContext) {
        AndroidSoundEffectPlayer(context.applicationContext)
    }
    val isLoading = uiState is UiState.Loading

    // --- Dialog state
    var showChangeSizeDialog by rememberSaveable { mutableStateOf(false) }
    var showWinDialog by rememberSaveable { mutableStateOf(false) }

    // Stepper local state
    var pendingSize by rememberSaveable { mutableIntStateOf(Constants.DEFAULT_COLUMNS_ROWS) }

    // Only show setup dialog once: if boardSize already exists (e.g. process restore), skip it
    LaunchedEffect(boardSize) {
        if (boardSize != null) didCompleteSetup = true
    }

    var showSetupDialog = !didCompleteSetup

    // Win dialog trigger (only once per solved)
    var prevSolved by rememberSaveable { mutableStateOf(false) }
    val isSolved = gameStatus is GameStatus.Solved
    LaunchedEffect(isSolved) {
        if (isSolved && !prevSolved) {
            showWinDialog = true
            soundPlayer.playSolved()
        }
        prevSolved = isSolved
    }

    // Existing effects: invalid board + move sounds
    LaunchedEffect(viewModel) {
        launch {
            viewModel.uiState.collect { state ->
                if (state is UiState.InvalidBoard) snackbarHostState.showSnackbar(state.message)
            }
        }
        launch {
            viewModel.events.collect { event ->
                when (event) {
                    is UiEvent.QueenPlaced -> soundPlayer.playQueenPlaced()
                    is UiEvent.QueenRemoved -> soundPlayer.playQueenRemoved()
                    is UiEvent.BoardReset -> soundPlayer.playStart()
                    is UiEvent.ConflictDetected -> soundPlayer.playConflict()
                }
            }
        }
    }

    // --- Setup Dialog (shown at app start)
    if (showSetupDialog) {
        BoardSizeDialog(
            title = "Choose board size",
            initial = pendingSize,
            min = 4,
            max = 20,
            confirmText = "Start",
            onDismiss = { /* Don't allow dismiss on initial setup */ },
            dismissEnabled = false,
            onConfirm = { size ->
                pendingSize = size
                viewModel.onSizeChanged(size)
                showSetupDialog = false
            }
        )
    }

    // --- Change size dialog (triggered by small button)
    if (showChangeSizeDialog) {
        BoardSizeDialog(
            title = "Change board size",
            initial = pendingSize,
            min = 4,
            max = 20,
            confirmText = "Apply",
            onDismiss = { showChangeSizeDialog = false },
            dismissEnabled = true,
            onConfirm = { size ->
                pendingSize = size
                viewModel.onSizeChanged(size) // resets game because solver.setBoardSize
                showChangeSizeDialog = false
            }
        )
    }

    // --- Win dialog
    if (showWinDialog) {
        val solved = gameStatus as? GameStatus.Solved
        if (solved != null) {
            WinDialog(
                solved = solved,
                elapsedMillis = elapsedMillis,
                onPlayAgain = {
                    showWinDialog = false
                    viewModel.resetGame()
                },
                onNextLevel = {
                    showWinDialog = false
                    val next = (boardSize ?: solved.size) + 1
                    viewModel.onSizeChanged(next)
                }
            )
        }
    }

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

            if (boardSize != null) {
                GameHud(
                    size = boardSize!!,
                    status = gameStatus,
                    elapsedMillis = elapsedMillis,
                    isLoading = isLoading,
                    onChange = {
                        pendingSize = boardSize ?: pendingSize
                        showChangeSizeDialog = true
                    },
                    onReset = { viewModel.resetGame() },
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
                    isLoading -> CircularProgressIndicator(color = cs.primary)

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
                        typography = typography,
                        cs = cs
                    )
                }
            }
        }
    }
}