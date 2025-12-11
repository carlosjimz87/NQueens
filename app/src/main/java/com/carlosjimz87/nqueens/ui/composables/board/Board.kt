package com.carlosjimz87.nqueens.ui.composables.board

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.carlosjimz87.nqueens.common.validateNQueensBoardSize
import com.carlosjimz87.nqueens.domain.error.BoardError

@Composable
fun Board(
    size: Int,
    modifier: Modifier = Modifier,
    onInvalidSize: (BoardError) -> Unit = {}
) {
    when (val result = validateNQueensBoardSize(size)) {
        BoardError.NoError -> {
            BoardPainter(size = size, modifier = modifier)
        }

        else -> {
            onInvalidSize(result)
        }
    }
}