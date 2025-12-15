package com.carlosjimz87.nqueens.ui.composables.conflicts

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import com.carlosjimz87.rules.model.Cell
import com.carlosjimz87.rules.model.Conflicts

@Composable
fun ConflictLines(
    size: Int,
    conflicts: Conflicts,
    color: Color,
    strokePx: Float,
    modifier: Modifier = Modifier
) {
    if (!conflicts.hasConflicts) return

    Canvas(modifier = modifier) {
        val cellW = this.size.width / size
        val cellH = this.size.height / size

        fun centerOf(c: Cell): Offset {
            val x = (c.col + 0.5f) * cellW
            val invertedRow = (size - 1 - c.row)
            val y = (invertedRow + 0.5f) * cellH
            return Offset(x, y)
        }

        conflicts.pairs.forEach { (aCell, bCell) ->
            drawLine(
                color = color,
                start = centerOf(aCell),
                end = centerOf(bCell),
                strokeWidth = strokePx,
                cap = StrokeCap.Round
            )
        }
    }
}