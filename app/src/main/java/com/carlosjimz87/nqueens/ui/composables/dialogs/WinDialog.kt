package com.carlosjimz87.nqueens.ui.composables.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Moving
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.carlosjimz87.nqueens.common.formatElapsed
import com.carlosjimz87.nqueens.ui.theme.NQueensTheme
import com.carlosjimz87.rules.model.GameStatus

@Composable
fun WinDialog(
    solved: GameStatus.Solved,
    elapsedMillis: Long,
    onPlayAgain: () -> Unit,
    onNextLevel: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    val titleStyle = with(LocalDensity.current) {
        typography.headlineMedium.copy(
            shadow = Shadow(
                color = cs.onSurface.copy(alpha = 0.5f),
                offset = Offset(0f, 1.dp.toPx()),
                blurRadius = 2.dp.toPx()
            )
        )
    }

    BaseDialog(
        dismissEnabled = false,
        onDismiss = {}, // modal
        header = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "You Won !",
                    style = titleStyle,
                    color = cs.primary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(14.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                "Time ${formatElapsed(elapsedMillis)}",
                                style = typography.bodyMedium
                            )
                        },
                        leadingIcon = { Icon(Icons.Outlined.Timer, contentDescription = null) }
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text("Queens = ${solved.size}", style = typography.bodyMedium) },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.EmojiEvents,
                                contentDescription = null
                            )
                        }
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text("Moves = ${solved.moves}", style = typography.bodyMedium) },
                        leadingIcon = { Icon(Icons.Outlined.Moving, contentDescription = null) }
                    )
                }
            }
        },
        body = {
            // opcional: si no quieres body, deja vacío
        },
        footer = {
            Button(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                onClick = onPlayAgain,
                shape = RoundedCornerShape(28.dp)
            ) { Text("Play again") }

            OutlinedButton(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                onClick = onNextLevel,
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, cs.primary)
            ) { Text("Next level") }
        }
    )
}

@Preview(
    name = "WinDialog · Light",
    showBackground = true,
    backgroundColor = 0xFFF8F8FF,
    widthDp = 360
)
@Composable
fun WinDialogPreviewLight() {
    NQueensTheme(darkTheme = false) {
        WinDialog(
            solved = GameStatus.Solved(size = 6, moves = 18),
            elapsedMillis = 3_000L,
            onPlayAgain = {},
            onNextLevel = {}
        )
    }
}

@Preview(
    name = "WinDialog · Dark",
    showBackground = true,
    backgroundColor = 0xFF1C2541,
    widthDp = 360
)
@Composable
fun WinDialogPreviewDark() {
    NQueensTheme(darkTheme = true) {
        WinDialog(
            solved = GameStatus.Solved(size = 8, moves = 42),
            elapsedMillis = 93_000L,
            onPlayAgain = {},
            onNextLevel = {}
        )
    }
}

@Preview(
    name = "WinDialog · Long time",
    showBackground = true,
    backgroundColor = 0xFFF8F8FF,
    widthDp = 360
)
@Composable
fun WinDialogPreviewLongTime() {
    NQueensTheme(darkTheme = false) {
        WinDialog(
            solved = GameStatus.Solved(size = 12, moves = 120),
            elapsedMillis = 3_726_000L,
            onPlayAgain = {},
            onNextLevel = {}
        )
    }
}