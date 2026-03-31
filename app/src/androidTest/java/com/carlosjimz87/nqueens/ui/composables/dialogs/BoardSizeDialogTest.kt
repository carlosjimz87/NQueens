package com.carlosjimz87.nqueens.ui.composables.dialogs

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI tests for BoardSizeDialog verifying visible text, actions and accessibility.
 */
class BoardSizeDialogTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `dialog shows title and confirm button`() {
        rule.setContent {
            BoardSizeDialog(
                title = "Choose board size",
                initial = 8,
                min = 4,
                max = 20,
                confirmText = "Start",
                dismissEnabled = false,
                onDismiss = {},
                onConfirm = {}
            )
        }

        rule.onNodeWithText("Choose board size").assertIsDisplayed()
        rule.onNodeWithText("Start").assertIsDisplayed()
    }

    @Test
    fun `confirm triggers callback`() {
        var confirmed = false

        rule.setContent {
            BoardSizeDialog(
                title = "Choose board size",
                initial = 8,
                min = 4,
                max = 20,
                confirmText = "Start",
                dismissEnabled = false,
                onDismiss = {},
                onConfirm = { confirmed = true }
            )
        }

        rule.onNodeWithText("Start").performClick()
        assert(confirmed)
    }
}
