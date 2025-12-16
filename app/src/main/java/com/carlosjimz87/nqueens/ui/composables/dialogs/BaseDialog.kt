package com.carlosjimz87.nqueens.ui.composables.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
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
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = { if (dismissEnabled) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = dismissEnabled,
            dismissOnClickOutside = dismissEnabled,
            usePlatformDefaultWidth = false // we control width/height
        )
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Constrain dialog surface size
            val dialogWidth = min(maxWidth, 520.dp)
            val dialogMaxHeight = min(maxHeight, 620.dp)

            // Compact mode for short heights (landscape phones)
            val compact = maxHeight < 420.dp

            val hPad = if (compact) 16.dp else 20.dp
            val vPadHeader = if (compact) 12.dp else 18.dp
            val vPadBody = if (compact) 12.dp else 18.dp
            val vPadFooter = if (compact) 10.dp else 14.dp

            Surface(
                modifier = Modifier
                    .width(dialogWidth)
                    .heightIn(max = dialogMaxHeight),
                shape = RoundedCornerShape(24.dp),
                tonalElevation = 6.dp,
                shadowElevation = 10.dp,
                color = cs.surface
            ) {
                Column(Modifier.fillMaxWidth()) {

                    // Header (fixed)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(cs.secondaryContainer)
                            .padding(horizontal = hPad, vertical = vPadHeader),
                        content = header
                    )

                    // Body (scrollable, takes remaining height)
                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .fillMaxWidth()
                            .verticalScroll(scrollState)
                            .padding(horizontal = hPad, vertical = vPadBody),
                        content = body
                    )

                    // Footer (fixed)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = vPadFooter),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        content = footer
                    )
                }
            }
        }
    }
}