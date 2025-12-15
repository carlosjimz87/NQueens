package com.carlosjimz87.nqueens.ui.composables.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.carlosjimz87.nqueens.common.formatElapsed
import com.carlosjimz87.nqueens.store.model.ScoreEntry

@Composable
fun StatsTable(
    rows: List<ScoreEntry>,
    modifier: Modifier = Modifier,
    maxHeight: Dp = 200.dp
) {
    val cs = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = cs.surfaceVariant
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header fijo
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("#", style = typography.labelMedium, color = cs.onSurfaceVariant, modifier = Modifier.width(24.dp))
                Text("Time", style = typography.labelMedium, color = cs.onSurfaceVariant, modifier = Modifier.weight(1f))
                Text("Moves", style = typography.labelMedium, color = cs.onSurfaceVariant, modifier = Modifier.width(56.dp))
            }

            Spacer(Modifier.height(8.dp))

            if (rows.isEmpty()) {
                Text("No results yet.", color = cs.onSurfaceVariant)
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = maxHeight),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    itemsIndexed(
                        items = rows,
                        key = { index, e -> e.id.ifBlank { "$index-${e.timeMillis}-${e.moves}" } }
                    ) { index, e ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${index + 1}", modifier = Modifier.width(24.dp))
                            Text(formatElapsed(e.timeMillis), modifier = Modifier.weight(1f))
                            Text("${e.moves}", modifier = Modifier.width(56.dp))
                        }
                    }
                }
            }
        }
    }
}