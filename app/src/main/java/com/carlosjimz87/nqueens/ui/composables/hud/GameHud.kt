package com.carlosjimz87.nqueens.ui.composables.hud

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.carlosjimz87.nqueens.R
import androidx.compose.ui.unit.dp
import com.carlosjimz87.nqueens.common.formatElapsed
import com.carlosjimz87.rules.model.GameStatus

@Composable
fun GameHud(
    size: Int,
    status: GameStatus?,
    elapsedMillis: Long,
    isLoading: Boolean,
    onChange: () -> Unit,
    onReset: () -> Unit,
    onStats: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    val line1 = stringResource(R.string.hud_time, formatElapsed(elapsedMillis))
    val line2 = when (status) {
        is GameStatus.NotStarted, null -> stringResource(R.string.hud_queens_zero, size)
        is GameStatus.InProgress -> stringResource(
            R.string.hud_queens_placed,
            status.queensPlaced,
            size
        )
        is GameStatus.Solved -> stringResource(R.string.hud_queens_solved, size, size)
    }
    val line3 = when (status) {
        is GameStatus.NotStarted, null -> stringResource(R.string.hud_place_first_queen)
        is GameStatus.InProgress -> stringResource(R.string.hud_conflicts, status.conflicts)
        is GameStatus.Solved -> stringResource(R.string.hud_solved_in_moves, status.moves)
    }

    val icon2 = Icons.Outlined.EmojiEvents
    val icon3 = when (status) {
        is GameStatus.InProgress -> Icons.Outlined.ReportProblem
        else -> Icons.Outlined.Flag
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = cs.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(cs.secondaryContainer)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.hud_level, size),
                    style = typography.titleLarge,
                    color = cs.onSecondaryContainer
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onStats,
                        enabled = !isLoading,
                        shape = RoundedCornerShape(28.dp),
                        border = BorderStroke(1.dp, cs.primary),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Leaderboard,
                            contentDescription = stringResource(R.string.hud_see_leaderboard)
                        )
                    }

                    OutlinedButton(
                        onClick = onChange,
                        enabled = !isLoading,
                        shape = RoundedCornerShape(28.dp),
                        border = BorderStroke(1.dp, cs.primary),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Tune,
                            contentDescription = stringResource(R.string.hud_change_board_size)
                        )
                    }

                    Button(
                        onClick = onReset,
                        enabled = !isLoading,
                        shape = RoundedCornerShape(28.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Refresh,
                            contentDescription = stringResource(R.string.hud_reset_game)
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                HudChip(Icons.Outlined.Timer, line1)
                HudChip(icon2, line2)
                HudChip(icon3, line3)
            }
        }
    }
}