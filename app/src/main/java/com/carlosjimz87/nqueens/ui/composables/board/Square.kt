package com.carlosjimz87.nqueens.ui.composables.board

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import com.carlosjimz87.nqueens.R
import com.carlosjimz87.nqueens.ui.theme.BoardDark
import com.carlosjimz87.nqueens.ui.theme.BoardLight

@Composable
fun Square(
    modifier: Modifier,
    isDarkSquare: Boolean,
    hasQueen: Boolean,
) {
    val backgroundColor = if (isDarkSquare) BoardDark else BoardLight
    var prevHasQueen by remember { mutableStateOf(hasQueen) }

    val iconScale = remember { Animatable(1f) }

    LaunchedEffect(hasQueen) {
        val becameTrue = !prevHasQueen && hasQueen

        if (becameTrue) {
            iconScale.snapTo(1f)
            iconScale.animateTo(1.15f, tween(110, easing = FastOutSlowInEasing))
            iconScale.animateTo(1f, tween(220, easing = LinearOutSlowInEasing))
        }
    }

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
                modifier = Modifier
                    .fillMaxSize(0.7f)
                    .graphicsLayer {
                        scaleX = iconScale.value
                        scaleY = iconScale.value
                    },
                tint = Color.Unspecified
            )
        }
    }
}