package com.carlosjimz87.nqueens.ui.screens.board

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.carlosjimz87.nqueens.presentation.board.viewmodel.BoardViewModel
import com.carlosjimz87.nqueens.ui.composables.board.Board
import com.carlosjimz87.nqueens.ui.composables.board.InvalidBoard

@Composable
fun BoardScreen(
    modifier: Modifier = Modifier,
    viewModel: BoardViewModel = viewModel()
) {
    val cs = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val boardSize by viewModel.boardSize.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.errors.collect { uiError ->
            snackbarHostState.showSnackbar(uiError.message)
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
            Text(
                text = "Current N = ${boardSize ?: "-"}",
                style = typography.titleMedium
            )

            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                Button(onClick = { viewModel.onSizeChanged(8) }) {
                    Text("N = 8 (valid)")
                }
                Button(onClick = { viewModel.onSizeChanged(6) }) {
                    Text("N = 6 (valid)")
                }
                Button(onClick = { viewModel.onSizeChanged(2) }) {
                    Text("N = 2 (invalid)")
                }
                Button(onClick = { viewModel.onSizeChanged(22) }) {
                    Text("N = 22 (invalid)")
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                boardSize?.let {
                    Board(
                        size = it,
                        modifier = Modifier.fillMaxSize()
                    )
                } ?: InvalidBoard(
                    modifier = Modifier.fillMaxWidth(),
                    typography = typography,
                    cs = cs
                )

            }
        }
    }
}