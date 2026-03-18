package com.carlosjimz87.nqueens

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented visual tests for [MainActivity] and [BoardScreen].
 *
 * These tests run on a real device or emulator and verify visible UI transitions and
 * interactions — ensuring that user actions produce the expected visual outcomes.
 *
 * Each test starts from a fresh Activity launch; the setup BoardSizeDialog always
 * appears because the ViewModel initialises with no board size.
 */
@RunWith(AndroidJUnit4::class)
class BoardScreenInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    /** Dismisses the mandatory setup dialog by clicking "Start" with the default size (4). */
    private fun startGame() {
        composeRule.onNodeWithText("Start").performClick()
        composeRule.waitForIdle()
    }

    @Test
    fun setup_dialog_appears_on_launch_and_board_renders_after_start() {
        // The setup dialog must be visible before confirming
        composeRule.onNodeWithText("Choose board size").assertExists()

        startGame()

        // After confirming, the HUD "Level 4" label should be visible
        composeRule.onNodeWithText("Level 4").assertExists()
    }

    @Test
    fun tapping_a_cell_places_a_queen_on_the_board() {
        startGame()

        // Board starts with no queens
        composeRule.onAllNodesWithContentDescription("Queen").assertCountEquals(0)

        // Tap the cell at row=0, col=1
        composeRule.onNodeWithTag("cell_0_1").performClick()
        composeRule.waitForIdle()

        // Exactly one queen must now be visible
        composeRule.onAllNodesWithContentDescription("Queen").assertCountEquals(1)
    }

    @Test
    fun reset_button_clears_all_placed_queens() {
        startGame()

        // Place two queens
        composeRule.onNodeWithTag("cell_0_1").performClick()
        composeRule.onNodeWithTag("cell_1_3").performClick()
        composeRule.waitForIdle()
        composeRule.onAllNodesWithContentDescription("Queen").assertCountEquals(2)

        // Tap the reset button (identified by its icon content description)
        composeRule.onNodeWithContentDescription("Reset game").performClick()
        composeRule.waitForIdle()

        // Board must be empty
        composeRule.onAllNodesWithContentDescription("Queen").assertCountEquals(0)
    }

    @Test
    fun solving_4x4_board_displays_win_dialog() {
        startGame()

        // Place the canonical 4×4 solution: (row=0,col=1),(row=1,col=3),(row=2,col=0),(row=3,col=2)
        composeRule.onNodeWithTag("cell_0_1").performClick()
        composeRule.onNodeWithTag("cell_1_3").performClick()
        composeRule.onNodeWithTag("cell_2_0").performClick()
        composeRule.onNodeWithTag("cell_3_2").performClick()
        composeRule.waitForIdle()

        // Wait for win animation to finish and the dialog to appear (up to 5 s)
        composeRule.waitUntil(timeoutMillis = 5_000L) {
            composeRule.onAllNodes(hasText("You Won!")).fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText("You Won!").assertExists()
        composeRule.onNodeWithText("Play again").assertExists()
        composeRule.onNodeWithText("Next level").assertExists()
    }
}
