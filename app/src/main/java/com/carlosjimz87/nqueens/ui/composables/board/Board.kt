package com.carlosjimz87.nqueens.ui.composables.board

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.min
import com.carlosjimz87.nqueens.ui.composables.conflicts.ConflictLines
import com.carlosjimz87.nqueens.ui.composables.squares.SquaresAndQueens
import com.carlosjimz87.rules.model.Cell
import com.carlosjimz87.rules.model.Conflicts

@Composable
fun Board(
    size: Int,
    queens: Set<Cell>,
    conflicts: Conflicts,
    onCellClick: (Cell) -> Unit,
    showQueens: Boolean,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val side = min(maxWidth, maxHeight)

        Box(Modifier.size(side)) {
            SquaresAndQueens(
                size = size,
                queens = queens,
                onCellClick = onCellClick,
                conflicts = conflicts,
                showQueens = showQueens
            )
            ConflictLines(
                size = size,
                conflicts = conflicts,
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.65f),
                strokePx = 3f,
                modifier = Modifier.matchParentSize()
            )
        }
    }
}