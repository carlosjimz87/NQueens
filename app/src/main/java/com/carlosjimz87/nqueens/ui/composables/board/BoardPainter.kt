package com.carlosjimz87.nqueens.ui.composables.board

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun BoardPainter(
    modifier: Modifier = Modifier,
    size: Int,
) {
    Box(modifier.aspectRatio(1f)) {
        Column(Modifier.fillMaxSize()) {
            for (rank in size - 1 downTo 0) {
                Row(modifier = Modifier.weight(1f)) {
                    for (file in 0 until size) {
                        val isDark = (file + rank) % 2 == 0
                        Square(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize(),
                            isDarkSquare = isDark
                        )
                    }
                }
            }
        }
    }
}