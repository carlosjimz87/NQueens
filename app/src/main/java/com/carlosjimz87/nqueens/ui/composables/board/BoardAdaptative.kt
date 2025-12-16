package com.carlosjimz87.nqueens.ui.composables.board

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.carlosjimz87.nqueens.ui.composables.hud.GameHud
import com.carlosjimz87.rules.model.Cell
import com.carlosjimz87.rules.model.Conflicts
import com.carlosjimz87.rules.model.GameStatus

@Composable
fun BoardAdaptative(
    boardSize: Int,
    isLoading: Boolean,
    queens: Set<Cell>,
    conflicts: Conflicts,
    gameStatus: GameStatus?,
    elapsedMillis: Long,
    allowClicks: Boolean,
    showQueens: Boolean,
    onCellClick: (Cell) -> Unit,
    onChange: () -> Unit,
    onReset: () -> Unit,
    onStats: () -> Unit,
    modifier: Modifier = Modifier,
    winOverlay: @Composable BoxScope.() -> Unit = {}
) {
    BoxWithConstraints(modifier.fillMaxSize()) {

        val useSplitLayout = maxWidth > maxHeight * 1.05f // Slightly favor landscape

        @Composable
        fun BoardSlot(modifier: Modifier) {
            Box(modifier, contentAlignment = Alignment.Center) {
                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    Board(
                        size = boardSize,
                        queens = queens,
                        conflicts = conflicts,
                        onCellClick = { if (allowClicks) onCellClick(it) },
                        showQueens = showQueens,
                        modifier = Modifier.fillMaxSize()
                    )
                    winOverlay()
                }
            }
        }

        if (!useSplitLayout) {
            // Portrait (mobile portrait + tablet portrait)
            Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GameHud(
                    size = boardSize,
                    status = gameStatus,
                    elapsedMillis = elapsedMillis,
                    isLoading = isLoading,
                    onChange = onChange,
                    onReset = onReset,
                    onStats = onStats,
                    modifier = Modifier.fillMaxWidth()
                )

                BoardSlot(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
        } else {
            // Landscape (mobile landscape + tablet landscape)
            Row(
                Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GameHud(
                    size = boardSize,
                    status = gameStatus,
                    elapsedMillis = elapsedMillis,
                    isLoading = isLoading,
                    onChange = onChange,
                    onReset = onReset,
                    onStats = onStats,
                    modifier = Modifier
                        .widthIn(max = 420.dp)
                        .fillMaxHeight()
                )

                BoardSlot(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        }
    }
}