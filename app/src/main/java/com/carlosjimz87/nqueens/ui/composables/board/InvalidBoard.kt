package com.carlosjimz87.nqueens.ui.composables.board

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.carlosjimz87.nqueens.R
import androidx.compose.ui.unit.dp

@Composable
fun InvalidBoard(
    modifier: Modifier,
    typography: Typography,
    cs: ColorScheme
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(id = R.string.board_not_available),
            style = typography.titleMedium,
            color = cs.onSurface,
        )
    }
}