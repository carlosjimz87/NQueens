package com.carlosjimz87.nqueens.ui.screens

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.carlosjimz87.nqueens.MainActivity
import com.carlosjimz87.nqueens.presentation.board.intent.BoardIntent
import com.carlosjimz87.nqueens.presentation.board.viewmodel.BoardViewModel
import com.carlosjimz87.rules.model.Cell
import org.junit.Rule
import org.junit.Test
import org.koin.java.KoinJavaComponent.getKoin

/**
 * End-to-end instrumented test for the full [MainActivity] with real Koin DI.
 *
 * Launches the actual activity (which initializes Koin via [NQueensApp]) and
 * exercises the complete user flow: setup dialog, queen placement, reset,
 * and board solving.
 *
 * Note: Individual board cells do not have testTags in the current production
 * composables, so queen placement is driven via ViewModel intent dispatch
 * rather than direct cell clicks. This is noted as a known limitation;
 * adding testTag("cell_row_col") to production code would enable pure
 * UI-driven tests.
 */
class BoardScreenInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    // -- Helpers --

    /**
     * Clicks the "Start" button on the initial board size dialog.
     * The dialog defaults to size 4 (DEFAULT_COLUMNS_ROWS).
     */
    private fun startGame() {
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Start").performClick()
        composeRule.waitForIdle()
    }

    /**
     * Retrieves the [BoardViewModel] from Koin to dispatch intents directly.
     * This is necessary because cells lack testTags for direct click targeting.
     */
    private fun viewModel(): BoardViewModel = getKoin().get()

    // -- Setup dialog --

    @Test
    fun setup_dialog_appears_on_launch() {
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Choose board size").assertIsDisplayed()
        composeRule.onNodeWithText("Start").assertIsDisplayed()
        composeRule.onNodeWithText("Select board size:").assertIsDisplayed()
    }

    @Test
    fun board_renders_after_clicking_start() {
        startGame()

        // After starting, the board should render with HUD elements
        composeRule.onNodeWithText("Level 4").assertIsDisplayed()
        composeRule.onNodeWithText("Queens 0/4").assertIsDisplayed()
        composeRule.onNodeWithText("Place your first queen").assertIsDisplayed()
    }

    // -- Queen placement --

    @Test
    fun placing_a_queen_updates_count_and_shows_queen_icon() {
        startGame()

        val vm = viewModel()

        // Initially no queens
        composeRule
            .onAllNodesWithContentDescription("Queen")
            .assertCountEquals(0)

        // Place a queen via ViewModel dispatch
        composeRule.runOnUiThread {
            vm.dispatch(BoardIntent.ClickCell(Cell(0, 0)))
        }
        composeRule.waitForIdle()

        // Verify queen appears
        composeRule
            .onAllNodesWithContentDescription("Queen")
            .assertCountEquals(1)
        composeRule.onNodeWithText("Queens 1/4").assertIsDisplayed()
    }

    // -- Reset --

    @Test
    fun reset_button_clears_all_placed_queens() {
        startGame()

        val vm = viewModel()

        // Place two queens
        composeRule.runOnUiThread {
            vm.dispatch(BoardIntent.ClickCell(Cell(0, 0)))
        }
        composeRule.waitForIdle()

        composeRule.runOnUiThread {
            vm.dispatch(BoardIntent.ClickCell(Cell(1, 2)))
        }
        composeRule.waitForIdle()

        composeRule
            .onAllNodesWithContentDescription("Queen")
            .assertCountEquals(2)

        // Click reset
        composeRule
            .onNodeWithContentDescription("Reset game")
            .performClick()
        composeRule.waitForIdle()

        // All queens should be cleared
        composeRule
            .onAllNodesWithContentDescription("Queen")
            .assertCountEquals(0)
        composeRule.onNodeWithText("Queens 0/4").assertIsDisplayed()
        composeRule.onNodeWithText("Place your first queen").assertIsDisplayed()
    }

    // -- Solving a 4x4 board --

    @Test
    fun solving_4x4_board_shows_win_dialog() {
        startGame()

        val vm = viewModel()

        // Canonical 4x4 solution: (0,1), (1,3), (2,0), (3,2)
        // No two queens share a row, column, or diagonal.
        val solution = listOf(
            Cell(row = 0, col = 1),
            Cell(row = 1, col = 3),
            Cell(row = 2, col = 0),
            Cell(row = 3, col = 2),
        )

        for (cell in solution) {
            composeRule.runOnUiThread {
                vm.dispatch(BoardIntent.ClickCell(cell))
            }
            composeRule.waitForIdle()
        }

        // Wait for win animation to complete and dialog to appear
        composeRule.waitUntil(timeoutMillis = 5_000L) {
            composeRule
                .onAllNodesWithText("You Won!")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // Verify win dialog content
        composeRule.onNodeWithText("You Won!").assertIsDisplayed()
        composeRule.onNodeWithText("Play again").assertIsDisplayed()
        composeRule.onNodeWithText("Next level").assertIsDisplayed()
    }

    @Test
    fun play_again_from_win_dialog_resets_the_board() {
        startGame()

        val vm = viewModel()

        // Solve the 4x4 board
        val solution = listOf(
            Cell(row = 0, col = 1),
            Cell(row = 1, col = 3),
            Cell(row = 2, col = 0),
            Cell(row = 3, col = 2),
        )
        for (cell in solution) {
            composeRule.runOnUiThread {
                vm.dispatch(BoardIntent.ClickCell(cell))
            }
            composeRule.waitForIdle()
        }

        composeRule.waitUntil(timeoutMillis = 5_000L) {
            composeRule
                .onAllNodesWithText("Play again")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // Click "Play again"
        composeRule.onNodeWithText("Play again").performClick()
        composeRule.waitForIdle()

        // Board should reset to initial state with same size
        composeRule.onNodeWithText("Queens 0/4").assertIsDisplayed()
        composeRule
            .onAllNodesWithContentDescription("Queen")
            .assertCountEquals(0)
    }
}
