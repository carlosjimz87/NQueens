package com.carlosjimz87.nqueens.ui.composables.board

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.carlosjimz87.nqueens.R
import com.carlosjimz87.nqueens.ui.theme.BoardDark
import com.carlosjimz87.nqueens.ui.theme.BoardLight

@Composable
fun Square(
    modifier: Modifier,
    isDarkSquare: Boolean,
    hasQueen: Boolean
) {
    val backgroundColor = if (isDarkSquare) BoardDark else BoardLight

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (hasQueen) {
            Icon(
                painter = painterResource(id = R.drawable.wq),
                contentDescription = "Queen",
                modifier = Modifier.fillMaxSize(0.7f),
                tint = Color.Unspecified
            )
        }
    }
}