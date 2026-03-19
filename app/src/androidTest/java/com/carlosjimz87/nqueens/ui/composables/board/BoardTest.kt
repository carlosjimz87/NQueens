package com.carlosjimz87.nqueens.ui.composables.board

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
import com.carlosjimz87.rules.model.ConflictPair
import com.carlosjimz87.rules.model.Conflicts
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI tests for the [Board] composable.
 *
 * Board wraps SquaresAndQueens + ConflictLines.
 * Tests verify:
 * - Correct queen icon rendering for given queens set
 * - Empty board renders without queens
 * - Click callback fires
 * - Conflict state does not prevent queen rendering
 */
class BoardTest {

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
                Board(
                    size = size,
                    queens = queens,
                    conflicts = conflicts,
                    onCellClick = onCellClick,
                    showQueens = showQueens,
                    modifier = Modifier.size(320.dp)
                )
            }
        }
    }

    // -- Rendering --

    @Test
    fun empty_board_has_no_queen_icons() {
        setContent(size = 4, queens = emptySet())

        composeTestRule
            .onAllNodesWithContentDescription("Queen")
            .assertCountEquals(0)
    }

    @Test
    fun board_with_one_queen_shows_one_queen_icon() {
        setContent(size = 4, queens = setOf(Cell(0, 0)))

        composeTestRule
            .onAllNodesWithContentDescription("Queen")
            .assertCountEquals(1)
    }

    @Test
    fun board_with_three_queens_shows_three_queen_icons() {
        setContent(
            size = 4,
            queens = setOf(Cell(0, 1), Cell(1, 3), Cell(2, 0))
        )

        composeTestRule
            .onAllNodesWithContentDescription("Queen")
            .assertCountEquals(3)
    }

    @Test
    fun queens_hidden_when_showQueens_false() {
        setContent(
            size = 4,
            queens = setOf(Cell(0, 0)),
            showQueens = false
        )

        composeTestRule
            .onAllNodesWithContentDescription("Queen")
            .assertCountEquals(0)
    }

    // -- Conflicts --

    @Test
    fun queens_still_visible_when_conflicting() {
        val q1 = Cell(0, 0)
        val q2 = Cell(0, 1)
        val conflicts = Conflicts(
            conflictsByCell = mapOf(q1 to setOf(q2), q2 to setOf(q1)),
            pairs = listOf(ConflictPair(q1, q2))
        )

        setContent(
            size = 4,
            queens = setOf(q1, q2),
            conflicts = conflicts
        )

        composeTestRule
            .onAllNodesWithContentDescription("Queen")
            .assertCountEquals(2)
    }

    // -- Click action --

    @Test
    fun clicking_queen_icon_triggers_callback() {
        val clicks = mutableListOf<Cell>()
        setContent(
            size = 4,
            queens = setOf(Cell(0, 0)),
            onCellClick = { clicks.add(it) }
        )

        composeTestRule
            .onAllNodesWithContentDescription("Queen")
            .onFirst()
            .performClick()

        assertTrue("Expected at least one click callback", clicks.isNotEmpty())
    }
}
