package com.carlosjimz87.nqueens.ui.composables.game

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun HudChip(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    val typography = MaterialTheme.typography

    AssistChip(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 40.dp), // altura estable
        onClick = {},
        label = {
            Text(
                text = text,
                style = typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingIcon = { Icon(icon, contentDescription = null) }
    )
}