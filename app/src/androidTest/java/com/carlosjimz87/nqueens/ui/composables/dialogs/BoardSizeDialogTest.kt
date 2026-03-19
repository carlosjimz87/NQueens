package com.carlosjimz87.nqueens.ui.composables.dialogs

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class BoardSizeDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // -- helpers --

    private fun setDialog(
        title: String = "Choose board size",
        initial: Int = 8,
        min: Int = 4,
        max: Int = 20,
        confirmText: String = "Start",
        dismissEnabled: Boolean = false,
        onDismiss: () -> Unit = {},
        onConfirm: (Int) -> Unit = {}
    ) {
        composeTestRule.setContent {
            MaterialTheme {
                BoardSizeDialog(
                    title = title,
                    initial = initial,
                    min = min,
                    max = max,
                    confirmText = confirmText,
                    dismissEnabled = dismissEnabled,
                    onDismiss = onDismiss,
                    onConfirm = onConfirm
                )
            }
        }
    }

    // ---- States ----

    @Test
    fun displays_title_and_initial_value() {
        setDialog(title = "Choose board size", initial = 8)

        composeTestRule.onNodeWithText("Choose board size").assertIsDisplayed()
        composeTestRule.onNodeWithText("8").assertIsDisplayed()
    }

    @Test
    fun shows_confirm_button_with_correct_text() {
        setDialog(confirmText = "Start")

        composeTestRule.onNodeWithText("Start").assertIsDisplayed()
    }

    @Test
    fun cancel_button_visible_when_dismissEnabled_is_true() {
        setDialog(dismissEnabled = true)

        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
    }

    @Test
    fun cancel_button_hidden_when_dismissEnabled_is_false() {
        setDialog(dismissEnabled = false)

        composeTestRule.onNodeWithText("Cancel").assertDoesNotExist()
    }

    // ---- Click actions ----

    @Test
    fun clicking_increase_increments_value() {
        setDialog(initial = 8)

        composeTestRule.onNodeWithContentDescription("Increase").performClick()

        composeTestRule.onNodeWithText("9").assertIsDisplayed()
    }

    @Test
    fun clicking_decrease_decrements_value() {
        setDialog(initial = 8)

        composeTestRule.onNodeWithContentDescription("Decrease").performClick()

        composeTestRule.onNodeWithText("7").assertIsDisplayed()
    }

    @Test
    fun clicking_confirm_calls_onConfirm_with_current_value() {
        var confirmed = -1
        setDialog(initial = 8, onConfirm = { confirmed = it })

        composeTestRule.onNodeWithText("Start").performClick()

        assertEquals(8, confirmed)
    }

    @Test
    fun clicking_confirm_after_increase_calls_onConfirm_with_incremented_value() {
        var confirmed = -1
        setDialog(initial = 8, onConfirm = { confirmed = it })

        composeTestRule.onNodeWithContentDescription("Increase").performClick()
        composeTestRule.onNodeWithText("Apply").assertDoesNotExist() // sanity: confirmText is "Start"
        composeTestRule.onNodeWithText("Start").performClick()

        assertEquals(9, confirmed)
    }

    @Test
    fun clicking_cancel_calls_onDismiss() {
        var dismissed = false
        setDialog(dismissEnabled = true, onDismiss = { dismissed = true })

        composeTestRule.onNodeWithText("Cancel").performClick()

        assertTrue(dismissed)
    }

    // ---- Boundary ----

    @Test
    fun decrease_button_disabled_at_min_value() {
        setDialog(initial = 4, min = 4)

        composeTestRule.onNodeWithContentDescription("Decrease").assertIsNotEnabled()
    }

    @Test
    fun increase_button_disabled_at_max_value() {
        setDialog(initial = 20, max = 20)

        composeTestRule.onNodeWithContentDescription("Increase").assertIsNotEnabled()
    }

    // ---- Accessibility ----

    @Test
    fun decrease_and_increase_buttons_have_content_descriptions() {
        setDialog()

        composeTestRule.onNodeWithContentDescription("Decrease").assertExists()
        composeTestRule.onNodeWithContentDescription("Increase").assertExists()
    }
}
