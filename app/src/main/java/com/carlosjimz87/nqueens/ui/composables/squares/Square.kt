package com.carlosjimz87.nqueens.ui.composables.squares

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.carlosjimz87.nqueens.R
import com.carlosjimz87.nqueens.ui.theme.BoardDark
import com.carlosjimz87.nqueens.ui.theme.BoardLight

@Composable
fun Square(
    modifier: Modifier,
    isDarkSquare: Boolean,
    hasQueen: Boolean,
    isConflicting: Boolean
) {
    val cs = MaterialTheme.colorScheme
    val base = if (isDarkSquare) BoardDark else BoardLight

    val overlayAlpha = if (isConflicting) 0.22f else 0f
    val strokeWidth = if (isConflicting) 2.dp else 0.dp

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
            .background(base)
            .drawWithContent {
                drawContent()
                if (isConflicting) {
                    drawRect(color = cs.error.copy(alpha = overlayAlpha))
                    val px = strokeWidth.toPx()
                    drawRect(
                        color = cs.error.copy(alpha = 0.85f),
                        style = Stroke(width = px)
                    )
                }
            },
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