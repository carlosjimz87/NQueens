package com.carlosjimz87.nqueens.ui.composables.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import com.carlosjimz87.nqueens.ui.theme.NQueensTheme

@Composable
fun BoardSizeDialog(
    title: String,
    initial: Int,
    min: Int,
    max: Int,
    confirmText: String,
    dismissEnabled: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    var value by rememberSaveable { mutableIntStateOf(initial.coerceIn(min, max)) }

    BaseDialog(
        dismissEnabled = dismissEnabled,
        onDismiss = onDismiss,
        header = {
            Text(title, style = typography.titleLarge, color = cs.onSecondaryContainer)
            Spacer(Modifier.height(10.dp))
            AssistChip(
                onClick = {},
                label = {
                    Text(
                        stringResource(R.string.board_size_picker_title, min, max),
                        style = typography.bodyMedium
                    )
                },
                leadingIcon = { Icon(Icons.Outlined.GridOn, contentDescription = null) }
            )
        },
        body = {
            Text(
                stringResource(R.string.board_size_select),
                style = typography.bodyMedium,
                color = cs.onSurfaceVariant
            )
            Spacer(Modifier.height(14.dp))

            Surface(shape = RoundedCornerShape(28.dp), color = cs.surfaceVariant) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(
                        onClick = { value = (value - 1).coerceAtLeast(min) },
                        enabled = value > min
                    ) {
                        Icon(
                            Icons.Outlined.Remove,
                            contentDescription = stringResource(R.string.board_size_decrease)
                        )
                    }

                    Text(value.toString(), style = typography.headlineMedium, color = cs.primary)

                    IconButton(
                        onClick = { value = (value + 1).coerceAtMost(max) },
                        enabled = value < max
                    ) {
                        Icon(
                            Icons.Outlined.Add,
                            contentDescription = stringResource(R.string.board_size_increase)
                        )
                    }
                }
            }
        },
        footer = {
            Button(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                onClick = { onConfirm(value) },
                shape = RoundedCornerShape(28.dp)
            ) { Text(confirmText) }

            if (dismissEnabled) {
                OutlinedButton(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    onClick = onDismiss,
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, cs.primary)
                ) { Text(stringResource(R.string.cancel)) }
            }
        }
    )
}

@Preview(
    name = "BoardSizeDialog · Setup · Light (modal)",
    showBackground = true,
    backgroundColor = 0xFFF8F8FF,
    widthDp = 360
)
@Composable
fun BoardSizeDialogPreviewSetupLight() {
    NQueensTheme(darkTheme = false) {
        BoardSizeDialog(
            title = stringResource(R.string.choose_board_size),
            initial = 8,
            min = 4,
            max = 20,
            confirmText = stringResource(id = R.string.start),
            dismissEnabled = false,
            onDismiss = {},
            onConfirm = {}
        )
    }
}

@Preview(
    name = "BoardSizeDialog · Change · Light (dismissible)",
    showBackground = true,
    backgroundColor = 0xFFF8F8FF,
    widthDp = 360
)
@Composable
fun BoardSizeDialogPreviewChangeLight() {
    NQueensTheme(darkTheme = false) {
        BoardSizeDialog(
            title = stringResource(R.string.change_board_size),
            initial = 6,
            min = 4,
            max = 20,
            confirmText = stringResource(id = R.string.apply),
            dismissEnabled = true,
            onDismiss = {},
            onConfirm = {}
        )
    }
}

@Preview(
    name = "BoardSizeDialog · Setup · Dark (modal)",
    showBackground = true,
    backgroundColor = 0xFF1C2541,
    widthDp = 360
)
@Composable
fun BoardSizeDialogPreviewSetupDark() {
    NQueensTheme(darkTheme = true) {
        BoardSizeDialog(
            title = stringResource(R.string.choose_board_size),
            initial = 8,
            min = 4,
            max = 20,
            confirmText = stringResource(id = R.string.start),
            dismissEnabled = false,
            onDismiss = {},
            onConfirm = {}
        )
    }
}

@Preview(
    name = "BoardSizeDialog · Edge case (min)",
    showBackground = true,
    backgroundColor = 0xFFF8F8FF,
    widthDp = 360
)
@Composable
fun BoardSizeDialogPreviewMin() {
    NQueensTheme(darkTheme = false) {
        BoardSizeDialog(
            title = stringResource(R.string.choose_board_size),
            initial = 4,
            min = 4,
            max = 20,
            confirmText = stringResource(id = R.string.start),
            dismissEnabled = false,
            onDismiss = {},
            onConfirm = {}
        )
    }
}

@Preview(
    name = "BoardSizeDialog · Edge case (max)",
    showBackground = true,
    backgroundColor = 0xFFF8F8FF,
    widthDp = 360
)
@Composable
fun BoardSizeDialogPreviewMax() {
    NQueensTheme(darkTheme = false) {
        BoardSizeDialog(
            title = stringResource(R.string.choose_board_size),
            initial = 20,
            min = 4,
            max = 20,
            confirmText = stringResource(id = R.string.start),
            dismissEnabled = true,
            onDismiss = {},
            onConfirm = {}
        )
    }
}