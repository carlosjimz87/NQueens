package com.carlosjimz87.nqueens.ui.composables.stats

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.carlosjimz87.nqueens.data.model.Leaderboards
import com.carlosjimz87.nqueens.data.model.ScoreEntry
import com.carlosjimz87.nqueens.ui.composables.dialogs.StatsDialog
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class StatsTableTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // -- helpers --

    private fun testEntry(
        id: String = "test-id",
        size: Int = 8,
        time: Long = 60_000L,
        moves: Int = 30,
        epoch: Long = 0L
    ): ScoreEntry = ScoreEntry(
        id = id,
        size = size,
        timeMillis = time,
        moves = moves,
        epochMillis = epoch
    )

    // ========================================================================
    // StatsTable tests
    // ========================================================================

    @Test
    fun empty_state_shows_no_results_message() {
        composeTestRule.setContent {
            MaterialTheme {
                StatsTable(rows = emptyList())
            }
        }

        composeTestRule.onNodeWithText("No results yet.").assertIsDisplayed()
    }

    @Test
    fun displays_header_row_with_rank_time_moves() {
        composeTestRule.setContent {
            MaterialTheme {
                StatsTable(rows = emptyList())
            }
        }

        composeTestRule.onNodeWithText("#").assertIsDisplayed()
        composeTestRule.onNodeWithText("Time").assertIsDisplayed()
        composeTestRule.onNodeWithText("Moves").assertIsDisplayed()
    }

    @Test
    fun displays_entries_with_correct_rank_and_formatted_time() {
        val entries = listOf(
            testEntry(id = "a", time = 65_000L, moves = 10),  // 01:05
            testEntry(id = "b", time = 130_000L, moves = 20)  // 02:10
        )

        composeTestRule.setContent {
            MaterialTheme {
                StatsTable(rows = entries)
            }
        }

        composeTestRule.onNodeWithText("1").assertIsDisplayed()
        composeTestRule.onNodeWithText("2").assertIsDisplayed()
        composeTestRule.onNodeWithText("01:05").assertIsDisplayed()
        composeTestRule.onNodeWithText("02:10").assertIsDisplayed()
    }

    @Test
    fun displays_entry_moves_count() {
        val entries = listOf(
            testEntry(id = "a", time = 60_000L, moves = 15),
            testEntry(id = "b", time = 90_000L, moves = 42)
        )

        composeTestRule.setContent {
            MaterialTheme {
                StatsTable(rows = entries)
            }
        }

        composeTestRule.onNodeWithText("15").assertIsDisplayed()
        composeTestRule.onNodeWithText("42").assertIsDisplayed()
    }

    // ========================================================================
    // StatsDialog tests
    // ========================================================================

    @Test
    fun displays_leaderboard_title() {
        composeTestRule.setContent {
            MaterialTheme {
                StatsDialog(
                    size = 8,
                    leaderboards = Leaderboards(byTime = emptyList(), byMoves = emptyList()),
                    onClose = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Leaderboard").assertIsDisplayed()
    }

    @Test
    fun displays_board_size_chip() {
        composeTestRule.setContent {
            MaterialTheme {
                StatsDialog(
                    size = 8,
                    leaderboards = Leaderboards(byTime = emptyList(), byMoves = emptyList()),
                    onClose = {}
                )
            }
        }

        composeTestRule.onNodeWithText("N = 8").assertIsDisplayed()
    }

    @Test
    fun displays_close_button() {
        composeTestRule.setContent {
            MaterialTheme {
                StatsDialog(
                    size = 8,
                    leaderboards = Leaderboards(byTime = emptyList(), byMoves = emptyList()),
                    onClose = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Close").assertIsDisplayed()
    }

    @Test
    fun clicking_close_calls_onClose_callback() {
        var closed = false

        composeTestRule.setContent {
            MaterialTheme {
                StatsDialog(
                    size = 8,
                    leaderboards = Leaderboards(byTime = emptyList(), byMoves = emptyList()),
                    onClose = { closed = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Close").performClick()

        assertTrue(closed)
    }

    @Test
    fun empty_leaderboard_shows_no_results() {
        composeTestRule.setContent {
            MaterialTheme {
                StatsDialog(
                    size = 8,
                    leaderboards = Leaderboards(byTime = emptyList(), byMoves = emptyList()),
                    onClose = {}
                )
            }
        }

        composeTestRule.onNodeWithText("No results yet.").assertIsDisplayed()
    }

    @Test
    fun populated_leaderboard_shows_entries() {
        val entries = listOf(
            testEntry(id = "1", time = 30_000L, moves = 10),
            testEntry(id = "2", time = 60_000L, moves = 20),
            testEntry(id = "3", time = 90_000L, moves = 30)
        )

        composeTestRule.setContent {
            MaterialTheme {
                StatsDialog(
                    size = 8,
                    leaderboards = Leaderboards(byTime = entries, byMoves = entries),
                    onClose = {}
                )
            }
        }

        composeTestRule.onNodeWithText("1").assertIsDisplayed()
        composeTestRule.onNodeWithText("2").assertIsDisplayed()
        composeTestRule.onNodeWithText("3").assertIsDisplayed()
    }
}
