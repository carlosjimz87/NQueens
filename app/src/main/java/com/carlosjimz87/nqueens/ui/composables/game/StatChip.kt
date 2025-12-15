package com.carlosjimz87.nqueens.ui.composables.game

import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.carlosjimz87.nqueens.ui.theme.GoldCrown
import com.carlosjimz87.nqueens.ui.theme.GoldDeep
import com.carlosjimz87.nqueens.ui.theme.QueenShadow

@Composable
fun StatChip(text: String, icon: ImageVector, warn: Boolean = false) {
    AssistChip(
        onClick = {},
        enabled = false,
        label = { Text(text) },
        leadingIcon = {
            Icon(icon, null, tint = if (warn) GoldDeep else GoldCrown)
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = QueenShadow.copy(alpha = 0.22f),
            labelColor = Color.White
        )
    )
}