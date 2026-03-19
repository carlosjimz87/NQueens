package com.carlosjimz87.nqueens.ui.composables.hud

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.carlosjimz87.rules.model.GameStatus
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class GameHudTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // -- Helper ----------------------------------------------------------

    private fun setContent(
        size: Int = 8,
        status: GameStatus? = null,
        elapsedMillis: Long = 0L,
        isLoading: Boolean = false,
        onChange: () -> Unit = {},
        onReset: () -> Unit = {},
        onStats: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            MaterialTheme {
                GameHud(
                    size = size,
                    status = status,
                    elapsedMillis = elapsedMillis,
                    isLoading = isLoading,
                    onChange = onChange,
                    onReset = onReset,
                    onStats = onStats,
                )
            }
        }
    }

    // -- States ----------------------------------------------------------

    @Test
    fun displays_level_with_correct_size() {
        setContent(size = 8)

        composeTestRule.onNodeWithText("Level 8").assertIsDisplayed()
    }

    @Test
    fun notStarted_shows_queens_zero_and_place_first_queen() {
        setContent(size = 8, status = GameStatus.NotStarted(8))

        composeTestRule.onNodeWithText("Queens 0/8").assertIsDisplayed()
        composeTestRule.onNodeWithText("Place your first queen").assertIsDisplayed()
    }

    @Test
    fun inProgress_shows_queens_placed_and_conflicts() {
        setContent(
            size = 8,
            status = GameStatus.InProgress(size = 8, queensPlaced = 3, conflicts = 1),
        )

        composeTestRule.onNodeWithText("Queens 3/8").assertIsDisplayed()
        composeTestRule.onNodeWithText("Conflicts 1").assertIsDisplayed()
    }

    @Test
    fun solved_shows_all_queens_and_solved_in_moves() {
        setContent(
            size = 6,
            status = GameStatus.Solved(size = 6, moves = 18),
        )

        composeTestRule.onNodeWithText("Queens 6/6").assertIsDisplayed()
        composeTestRule.onNodeWithText("Solved in 18 moves").assertIsDisplayed()
    }

    @Test
    fun displays_formatted_elapsed_time() {
        setContent(elapsedMillis = 65_000L)

        composeTestRule.onNodeWithText("Time 01:05").assertIsDisplayed()
    }

    // -- Click actions ---------------------------------------------------

    @Test
    fun clicking_stats_button_calls_onStats() {
        var called = false
        setContent(onStats = { called = true })

        composeTestRule.onNodeWithContentDescription("See leaderboard").performClick()

        assertTrue(called)
    }

    @Test
    fun clicking_change_button_calls_onChange() {
        var called = false
        setContent(onChange = { called = true })

        composeTestRule.onNodeWithContentDescription("Change board size").performClick()

        assertTrue(called)
    }

    @Test
    fun clicking_reset_button_calls_onReset() {
        var called = false
        setContent(onReset = { called = true })

        composeTestRule.onNodeWithContentDescription("Reset game").performClick()

        assertTrue(called)
    }

    // -- Loading state ---------------------------------------------------

    @Test
    fun buttons_disabled_when_isLoading_true() {
        setContent(isLoading = true)

        composeTestRule.onNodeWithContentDescription("See leaderboard").assertIsNotEnabled()
        composeTestRule.onNodeWithContentDescription("Change board size").assertIsNotEnabled()
        composeTestRule.onNodeWithContentDescription("Reset game").assertIsNotEnabled()
    }

    // -- Accessibility ---------------------------------------------------

    @Test
    fun all_action_buttons_have_content_descriptions() {
        setContent()

        composeTestRule.onNodeWithContentDescription("See leaderboard").assertExists()
        composeTestRule.onNodeWithContentDescription("Change board size").assertExists()
        composeTestRule.onNodeWithContentDescription("Reset game").assertExists()
    }
}
