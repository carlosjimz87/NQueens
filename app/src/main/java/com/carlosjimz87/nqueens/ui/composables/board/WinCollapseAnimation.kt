package com.carlosjimz87.nqueens.ui.composables.board

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.fontscaling.MathUtils.lerp
import com.carlosjimz87.nqueens.R
import com.carlosjimz87.rules.model.Cell
import kotlin.math.roundToInt

@Composable
fun WinAnimation(
    token: Int,
    size: Int,
    queens: List<Cell>,
    enabled: Boolean,
    onFinished: () -> Unit,
) {
    if (!enabled || queens.isEmpty()) return

    val progress = remember { Animatable(0f) }

    LaunchedEffect(token) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing)
        )
        onFinished()
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val maxW = constraints.maxWidth.toFloat()
        val maxH = constraints.maxHeight.toFloat()

        // Real board square inside this box
        val boardSidePx = minOf(maxW, maxH)
        val originX = (maxW - boardSidePx) / 2f
        val originY = (maxH - boardSidePx) / 2f

        val cellPx = boardSidePx / size
        val iconPx = cellPx * 0.70f
        val iconDp = with(LocalDensity.current) { iconPx.toDp() }

        // Geometrical center of the board square
        val boardCenter = Offset(
            x = originX + boardSidePx / 2f,
            y = originY + boardSidePx / 2f
        )

        queens.forEach { cell ->
            // Top-left of icon inside the board square + origin offset
            val x0 = originX + cell.col * cellPx + (cellPx - iconPx) / 2f
            val y0 = originY + (size - 1 - cell.row) * cellPx + (cellPx - iconPx) / 2f

            // Center-to-center lerp
            val iconCenter0 = Offset(x0 + iconPx / 2f, y0 + iconPx / 2f)

            val t = progress.value
            val cx = lerp(iconCenter0.x, boardCenter.x, t)
            val cy = lerp(iconCenter0.y, boardCenter.y, t)

            // Back to top-left
            val x = cx - iconPx / 2f
            val y = cy - iconPx / 2f

            Icon(
                painter = painterResource(id = R.drawable.wq),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier
                    .offset { IntOffset(x.roundToInt(), y.roundToInt()) }
                    .size(iconDp)
                    .graphicsLayer {
                        val s = 1f + 0.06f * t
                        scaleX = s
                        scaleY = s
                    }
            )
        }
    }
}