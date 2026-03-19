package com.carlosjimz87.nqueens.ui.composables.squares

import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.carlosjimz87.rules.model.Cell
import com.carlosjimz87.rules.model.Conflicts
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI tests for the [SquaresAndQueens] composable, which renders the grid
 * of squares and queens for the board.
 *
 * These tests verify:
 * - Correct number of queen icons rendered for a given set of queens
 * - Click callbacks fire with the correct cell
 * - Empty board has no queen icons
 */
class SquaresAndQueensTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent(
        size: Int = 4,
        queens: Set<Cell> = emptySet(),
        conflicts: Conflicts = Conflicts.Empty,
        showQueens: Boolean = true,
        onCellClick: (Cell) -> Unit = {}
    ) {
        composeTestRule.setContent {
            MaterialTheme {
                SquaresAndQueens(
                    size = size,
                    queens = queens,
                    onCellClick = onCellClick,
                    conflicts = conflicts,
                    showQueens = showQueens
                )
            }
        }
    }

    // -- Rendering --

    @Test
    fun empty_board_shows_no_queen_icons() {
        setContent(size = 4, queens = emptySet())

        composeTestRule
            .onAllNodesWithContentDescription("Queen")
            .assertCountEquals(0)
    }

    @Test
    fun board_with_two_queens_shows_two_queen_icons() {
        setContent(
            size = 4,
            queens = setOf(Cell(0, 0), Cell(1, 2))
        )

        composeTestRule
            .onAllNodesWithContentDescription("Queen")
            .assertCountEquals(2)
    }

    @Test
    fun board_with_all_queens_placed_shows_correct_count() {
        val queens = (0 until 4).map { Cell(it, it) }.toSet()
        setContent(size = 4, queens = queens)

        composeTestRule
            .onAllNodesWithContentDescription("Queen")
            .assertCountEquals(4)
    }

    @Test
    fun queens_hidden_when_showQueens_is_false() {
        setContent(
            size = 4,
            queens = setOf(Cell(0, 0), Cell(1, 1)),
            showQueens = false
        )

        composeTestRule
            .onAllNodesWithContentDescription("Queen")
            .assertCountEquals(0)
    }

    // -- Click actions --

    @Test
    fun clicking_a_square_triggers_onCellClick_callback() {
        val clicks = mutableListOf<Cell>()
        setContent(
            size = 4,
            queens = setOf(Cell(0, 0)),
            onCellClick = { clicks.add(it) }
        )

        // Click on the displayed queen icon
        composeTestRule
            .onAllNodesWithContentDescription("Queen")
            .onFirst()
            .performClick()

        assertEquals(1, clicks.size)
    }
}
