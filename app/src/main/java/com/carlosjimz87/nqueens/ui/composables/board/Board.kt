package com.carlosjimz87.nqueens.ui.composables.board

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.carlosjimz87.nqueens.domain.model.Cell

@Composable
fun Board(
    modifier: Modifier = Modifier,
    size: Int,
    queens: Set<Cell>,
    onCellClick: (Cell) -> Unit,
) {

    Box(modifier.aspectRatio(1f)) {
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
                            hasQueen = hasQueen
                        )
                    }
                }
            }
        }
    }
}