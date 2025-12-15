package com.carlosjimz87.nqueens.ui.composables.squares

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.carlosjimz87.rules.model.Cell
import com.carlosjimz87.rules.model.Conflicts

@Composable
fun SquaresAndQueens(
    size: Int,
    queens: Set<Cell>,
    onCellClick: (Cell) -> Unit,
    conflicts: Conflicts,
    showQueens: Boolean
) {
    Column(Modifier.fillMaxSize()) {
        for (rank in size - 1 downTo 0) {
            Row(modifier = Modifier.weight(1f)) {
                for (file in 0 until size) {
                    val cell = Cell(row = rank, col = file)
                    val isDark = (file + rank) % 2 == 0
                    val hasQueen = cell in queens

                    Square(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clickable { onCellClick(cell) },
                        isDarkSquare = isDark,
                        hasQueen = hasQueen,
                        showQueen = showQueens,
                        isConflicting = cell in conflicts.conflictCells
                    )
                }
            }
        }
    }
}