package com.carlosjimz87.nqueens.ui.screens.board

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ColorDemoScreen(
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "N-Queens Color Palette",
            style = MaterialTheme.typography.titleLarge
        )

        ColorRow("Primary", cs.primary, cs.onPrimary)
        ColorRow("PrimaryContainer", cs.primaryContainer, cs.onPrimaryContainer)
        ColorRow("Secondary", cs.secondary, cs.onSecondary)
        ColorRow("Tertiary", cs.tertiary, cs.onTertiary)
        ColorRow("Background", cs.background, cs.onBackground)
        ColorRow("Surface", cs.surface, cs.onBackground)
        ColorRow("SurfaceVariant", cs.surfaceVariant, cs.onSurfaceVariant)
        ColorRow("GoldQueen", cs.surfaceVariant, cs.onSurfaceVariant)
    }
}

@Composable
private fun ColorRow(
    label: String,
    color: androidx.compose.ui.graphics.Color,
    onColor: androidx.compose.ui.graphics.Color,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = label,
                color = onColor,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}