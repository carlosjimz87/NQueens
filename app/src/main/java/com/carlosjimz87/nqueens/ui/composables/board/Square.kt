package com.carlosjimz87.nqueens.ui.composables.board

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.carlosjimz87.nqueens.ui.theme.BoardDark
import com.carlosjimz87.nqueens.ui.theme.BoardLight

@Composable
fun Square(
    modifier: Modifier,
    isDarkSquare: Boolean) {
    Canvas(modifier = modifier.fillMaxSize(1f)) {
        drawRect(color = if (isDarkSquare) BoardDark else BoardLight)
    }    
}