package com.carlosjimz87.nqueens.ui.composables.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.carlosjimz87.nqueens.common.formatElapsed
import com.carlosjimz87.nqueens.domain.model.GameStatus

@Composable
fun GameStatusBar(
    status: GameStatus?,
    elapsedMillis: Long,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = cs.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // Estado principal
            val title = when (status) {
                is GameStatus.NotStarted -> "Ready to start"
                is GameStatus.InProgress -> "Game in progress"
                is GameStatus.Solved -> "Puzzle solved!"
                null -> ""
            }

            Text(
                text = title,
                style = typography.titleMedium,
                color = cs.onSurfaceVariant
            )

            // Línea de detalles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tiempo
                AssistChip(
                    onClick = {},
                    label = {
                        Text("Time ${formatElapsed(elapsedMillis)}")
                    }
                )

                when (status) {
                    is GameStatus.NotStarted -> {
                        AssistChip(
                            onClick = {},
                            label = { Text("N = ${status.size}") }
                        )
                    }

                    is GameStatus.InProgress -> {
                        AssistChip(
                            onClick = {},
                            label = {
                                Text("Queens ${status.queensPlaced}/${status.size}")
                            }
                        )
                        AssistChip(
                            onClick = {},
                            label = { Text("Conflicts ${status.conflicts}") }
                        )
                    }

                    is GameStatus.Solved -> {
                        AssistChip(
                            onClick = {},
                            label = { Text("Moves ${status.moves}") }
                        )
                    }

                    null -> Unit
                }
            }
        }
    }
}