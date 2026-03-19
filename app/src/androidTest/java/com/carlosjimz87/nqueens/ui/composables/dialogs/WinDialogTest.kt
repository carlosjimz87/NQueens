package com.carlosjimz87.nqueens.ui.composables.dialogs

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.carlosjimz87.rules.model.GameStatus
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class WinDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // -- helpers --

    private fun setContent(
        solved: GameStatus.Solved = GameStatus.Solved(size = 6, moves = 18),
        elapsedMillis: Long = 3_000L,
        rankByTime: Int = 2,
        rankByMoves: Int = 5,
        onShowStats: () -> Unit = {},
        onPlayAgain: () -> Unit = {},
        onNextLevel: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            MaterialTheme {
                WinDialog(
                    solved = solved,
                    elapsedMillis = elapsedMillis,
                    rankByTime = rankByTime,
                    rankByMoves = rankByMoves,
                    onShowStats = onShowStats,
                    onPlayAgain = onPlayAgain,
                    onNextLevel = onNextLevel,
                )
            }
        }
    }

    // -- States --

    @Test
    fun displays_You_Won_title() {
        setContent()

        composeTestRule
            .onNodeWithText("You Won!")
            .assertIsDisplayed()
    }

    @Test
    fun displays_time_chip_with_formatted_elapsed() {
        setContent(elapsedMillis = 3_000L)

        composeTestRule
            .onNodeWithText("Time 00:03", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun displays_queens_chip_with_size() {
        setContent(solved = GameStatus.Solved(size = 6, moves = 18))

        composeTestRule
            .onNodeWithText("Queens = 6", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun displays_moves_chip_with_move_count() {
        setContent(solved = GameStatus.Solved(size = 6, moves = 18))

        composeTestRule
            .onNodeWithText("Moves = 18", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun displays_rank_chip_when_ranks_are_positive() {
        setContent(rankByTime = 2, rankByMoves = 5)

        composeTestRule
            .onNodeWithText("Rank", substring = true)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("#2", substring = true)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("#5", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun hides_rank_chip_when_both_ranks_are_zero() {
        setContent(rankByTime = 0, rankByMoves = 0)

        composeTestRule
            .onNodeWithText("Rank", substring = true)
            .assertDoesNotExist()
    }

    // -- Click actions --

    @Test
    fun clicking_Play_again_calls_onPlayAgain() {
        var called = false
        setContent(onPlayAgain = { called = true })

        composeTestRule
            .onNodeWithText("Play again")
            .performClick()

        assertTrue("onPlayAgain should have been called", called)
    }

    @Test
    fun clicking_Next_level_calls_onNextLevel() {
        var called = false
        setContent(onNextLevel = { called = true })

        composeTestRule
            .onNodeWithText("Next level")
            .performClick()

        assertTrue("onNextLevel should have been called", called)
    }

    @Test
    fun clicking_See_Leaderboard_calls_onShowStats() {
        var called = false
        setContent(onShowStats = { called = true })

        composeTestRule
            .onNodeWithText("See Leaderboard")
            .performClick()

        assertTrue("onShowStats should have been called", called)
    }

    // -- Accessibility --

    @Test
    fun all_action_buttons_are_displayed_and_clickable() {
        var playAgainCalled = false
        var nextLevelCalled = false
        var showStatsCalled = false

        setContent(
            onPlayAgain = { playAgainCalled = true },
            onNextLevel = { nextLevelCalled = true },
            onShowStats = { showStatsCalled = true },
        )

        composeTestRule
            .onNodeWithText("Play again")
            .assertIsDisplayed()
            .assertIsEnabled()

        composeTestRule
            .onNodeWithText("Next level")
            .assertIsDisplayed()
            .assertIsEnabled()

        composeTestRule
            .onNodeWithText("See Leaderboard")
            .assertIsDisplayed()
            .assertIsEnabled()

        // Verify none were accidentally triggered by assertion
        assertFalse("onPlayAgain should not be called by asserting", playAgainCalled)
        assertFalse("onNextLevel should not be called by asserting", nextLevelCalled)
        assertFalse("onShowStats should not be called by asserting", showStatsCalled)
    }
}
