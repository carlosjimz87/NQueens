package com.carlosjimz87.nqueens.ui.composables.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun BaseDialog(
    dismissEnabled: Boolean,
    onDismiss: () -> Unit,
    header: @Composable ColumnScope.() -> Unit,
    body: @Composable ColumnScope.() -> Unit,
    footer: @Composable RowScope.() -> Unit,
) {
    val cs = MaterialTheme.colorScheme

    Dialog(
        onDismissRequest = { if (dismissEnabled) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = dismissEnabled,
            dismissOnClickOutside = dismissEnabled
        )
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 6.dp,
            shadowElevation = 10.dp,
            color = cs.surface
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {

                // Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(cs.secondaryContainer)
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    content = header
                )

                // Body
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    content = body
                )

                // Footer / Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    content = footer
                )
            }
        }
    }
}