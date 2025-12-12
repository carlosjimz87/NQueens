package com.carlosjimz87.nqueens.ui.composables.board

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.carlosjimz87.nqueens.ui.composables.conflicts.ConflictLinesOverlay
import com.carlosjimz87.nqueens.ui.composables.squares.SquaresAndQueens
import com.carlosjimz87.rules.model.Cell
import com.carlosjimz87.rules.model.Conflicts

@Composable
fun Board(
    size: Int,
    queens: Set<Cell>,
    conflicts: Conflicts,
    onCellClick: (Cell) -> Unit,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme

    Box(modifier.aspectRatio(1f)) {

        SquaresAndQueens(
            size = size,
            queens = queens,
            onCellClick = onCellClick,
            conflicts = conflicts
        )

        ConflictLinesOverlay(
            size = size,
            conflicts = conflicts,
            color = cs.error.copy(alpha = 0.65f),
            strokePx = 3f,
            modifier = Modifier.matchParentSize()
        )
    }
}