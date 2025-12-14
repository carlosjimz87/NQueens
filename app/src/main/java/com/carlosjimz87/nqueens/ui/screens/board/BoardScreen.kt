package com.carlosjimz87.nqueens.ui.screens.board

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import com.carlosjimz87.rules.model.GameStatus
import com.carlosjimz87.nqueens.presentation.audio.AndroidSoundEffectPlayer
import com.carlosjimz87.nqueens.presentation.board.event.UiEvent
import com.carlosjimz87.nqueens.presentation.board.state.UiState
import com.carlosjimz87.nqueens.presentation.board.viewmodel.BoardViewModel
import com.carlosjimz87.nqueens.ui.composables.board.Board
import com.carlosjimz87.nqueens.ui.composables.board.InvalidBoard
import com.carlosjimz87.nqueens.ui.composables.game.GameStatusBar
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

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val soundPlayer = remember { AndroidSoundEffectPlayer(context) }
    val isLoading = uiState is UiState.Loading

    var prevSolved by rememberSaveable { mutableStateOf(false) }
    val isSolved = gameStatus is GameStatus.Solved

    LaunchedEffect(Unit) {
        launch {
            viewModel.uiState.collect { state ->
                if (state is UiState.InvalidBoard) {
                    snackbarHostState.showSnackbar(state.message)
                }
            }
        }

        launch {
            viewModel.events.collect { event ->
                when (event) {
                    is UiEvent.QueenPlaced -> soundPlayer.playQueenPlaced()
                    is UiEvent.QueenRemoved -> soundPlayer.playQueenRemoved()
                    is UiEvent.BoardReset -> {
                        soundPlayer.playStart()
                        prevSolved = false
                    }
                    is UiEvent.ConflictDetected -> soundPlayer.playConflict()
                }
            }
        }
    }

    LaunchedEffect(isSolved) {
        if (isSolved && !prevSolved) {
            soundPlayer.playSolved()
        }
        prevSolved = isSolved
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
            Text(
                text = "Current N = ${boardSize ?: "-"}",
                style = typography.titleMedium
            )

            GameStatusBar(
                status = gameStatus,
                elapsedMillis = elapsedMillis,
                modifier = Modifier.fillMaxWidth()
            )

            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                Button(onClick = { viewModel.onSizeChanged(8) }) { Text("N = 8 (valid)") }
                Button(onClick = { viewModel.onSizeChanged(6) }) { Text("N = 6 (valid)") }
                Button(onClick = { viewModel.onSizeChanged(2) }) { Text("N = 2 (invalid)") }
                Button(onClick = { viewModel.onSizeChanged(22) }) { Text("N = 22 (invalid)") }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isLoading -> CircularProgressIndicator()

                    boardSize != null -> {
                        Board(
                            size = boardSize!!,
                            queens = queens,
                            conflicts = conflicts,
                            onCellClick = viewModel::onCellClicked,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    else -> {
                        InvalidBoard(
                            modifier = Modifier.fillMaxWidth(),
                            typography = typography,
                            cs = cs
                        )
                    }
                }
            }
        }
    }
}