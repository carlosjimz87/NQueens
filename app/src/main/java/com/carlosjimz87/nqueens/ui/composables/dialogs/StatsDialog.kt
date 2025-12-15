package com.carlosjimz87.nqueens.ui.composables.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.Moving
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.carlosjimz87.nqueens.common.previewLeaderboardsNormal
import com.carlosjimz87.nqueens.common.previewLeaderboardsStress
import com.carlosjimz87.nqueens.store.model.Leaderboards
import com.carlosjimz87.nqueens.store.model.ScoreEntry
import com.carlosjimz87.nqueens.ui.composables.stats.StatsTable
import com.carlosjimz87.nqueens.ui.theme.NQueensTheme

@Composable
fun StatsDialog(
    size: Int,
    leaderboards: Leaderboards,
    onClose: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    var tab by rememberSaveable { mutableIntStateOf(0) } // 0 time, 1 moves

    Dialog(
        onDismissRequest = { /* modal */ },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 6.dp,
            shadowElevation = 10.dp,
            color = cs.surface
        ) {
            Column(Modifier.fillMaxWidth()) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(cs.secondaryContainer)
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Leaderboard", style = typography.titleLarge, color = cs.onSecondaryContainer)
                    Spacer(Modifier.height(10.dp))
                    AssistChip(
                        onClick = {},
                        label = { Text("N = $size", style = typography.bodyMedium) },
                        leadingIcon = { Icon(Icons.Outlined.GridOn, contentDescription = null) }
                    )
                }

                val rows = if (tab == 0) leaderboards.byTime else leaderboards.byMoves

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    StatsTable(
                        rows = rows,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(Modifier.height(12.dp))

                    Button(
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        onClick = onClose,
                        shape = RoundedCornerShape(28.dp)
                    ) { Text("Close") }
                }
            }
        }
    }
}

// --- Previews ---
@Preview(
    name = "StatsDialog · Light",
    showBackground = true,
    backgroundColor = 0xFFF8F8FF,
    widthDp = 360
)
@Composable
fun StatsDialogPreviewLight() {
    NQueensTheme(darkTheme = false) {
        StatsDialog(
            size = 8,
            leaderboards = previewLeaderboardsNormal(),
            onClose = {}
        )
    }
}

@Preview(
    name = "StatsDialog · Dark",
    showBackground = true,
    backgroundColor = 0xFF1C2541,
    widthDp = 360
)
@Composable
fun StatsDialogPreviewDark() {
    NQueensTheme(darkTheme = true) {
        StatsDialog(
            size = 10,
            leaderboards = previewLeaderboardsNormal(),
            onClose = {}
        )
    }
}

@Preview(
    name = "StatsDialog · Empty",
    showBackground = true,
    backgroundColor = 0xFFF8F8FF,
    widthDp = 360
)
@Composable
fun StatsDialogPreviewEmpty() {
    NQueensTheme(darkTheme = false) {
        StatsDialog(
            size = 6,
            leaderboards = Leaderboards(
                byTime = emptyList(),
                byMoves = emptyList()
            ),
            onClose = {}
        )
    }
}

@Preview(
    name = "StatsDialog · Stress",
    showBackground = true,
    backgroundColor = 0xFFF8F8FF,
    widthDp = 360
)
@Composable
fun StatsDialogPreviewStress() {
    NQueensTheme(darkTheme = false) {
        StatsDialog(
            size = 20,
            leaderboards = previewLeaderboardsStress(),
            onClose = {}
        )
    }
}