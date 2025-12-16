package com.carlosjimz87.nqueens.ui.composables.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.carlosjimz87.nqueens.R
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.carlosjimz87.nqueens.common.previewLeaderboardsNormal
import com.carlosjimz87.nqueens.common.previewLeaderboardsStress
import com.carlosjimz87.nqueens.data.model.Leaderboards
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
    var tab by rememberSaveable { mutableIntStateOf(0) }

    val rows = if (tab == 0) leaderboards.byTime else leaderboards.byMoves

    BaseDialog(
        dismissEnabled = false,
        onDismiss = {},
        header = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.leaderboard), style = typography.titleLarge, color = cs.onSecondaryContainer)
                Spacer(Modifier.height(10.dp))
                AssistChip(
                    onClick = {},
                    label = { Text(stringResource(R.string.n_equals, size), style = typography.bodyMedium) },
                    leadingIcon = { Icon(Icons.Outlined.GridOn, contentDescription = null) }
                )
                Spacer(Modifier.height(10.dp))
            }
        },
        body = {
            StatsTable(
                rows = rows,
                modifier = Modifier.fillMaxWidth()
            )
        },
        footer = {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 44.dp),
                onClick = onClose,
                shape = RoundedCornerShape(28.dp)
            ) { Text(stringResource(R.string.close)) }
        }
    )
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