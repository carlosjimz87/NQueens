package com.carlosjimz87.nqueens.ui.composables.squares

import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI tests for the [Square] composable.
 *
 * The Square composable renders:
 * - A dark or light background based on [isDarkSquare]
 * - A queen icon when [hasQueen] && [showQueen] are both true
 * - A conflict overlay when [isConflicting] is true
 *
 * Note: Click handling is not part of Square itself (it is applied by SquaresAndQueens
 * via a Modifier.clickable on the Square's modifier parameter).
 */
class SquareTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent(
        isDarkSquare: Boolean = false,
        hasQueen: Boolean = false,
        showQueen: Boolean = true,
        isConflicting: Boolean = false,
    ) {
        composeTestRule.setContent {
            MaterialTheme {
                Square(
                    modifier = Modifier.size(80.dp),
                    isDarkSquare = isDarkSquare,
                    hasQueen = hasQueen,
                    showQueen = showQueen,
                    isConflicting = isConflicting,
                )
            }
        }
    }

    // -- Queen visibility --

    @Test
    fun empty_square_shows_no_queen() {
        setContent(hasQueen = false, showQueen = true)

        composeTestRule
            .onNodeWithContentDescription("Queen")
            .assertDoesNotExist()
    }

    @Test
    fun square_with_queen_and_showQueen_true_shows_queen_icon() {
        setContent(hasQueen = true, showQueen = true)

        composeTestRule
            .onNodeWithContentDescription("Queen")
            .assertIsDisplayed()
    }

    @Test
    fun square_with_queen_but_showQueen_false_hides_queen_icon() {
        setContent(hasQueen = true, showQueen = false)

        composeTestRule
            .onNodeWithContentDescription("Queen")
            .assertDoesNotExist()
    }

    // -- Conflicting state --

    @Test
    fun conflicting_square_with_queen_still_shows_queen() {
        setContent(hasQueen = true, showQueen = true, isConflicting = true)

        composeTestRule
            .onNodeWithContentDescription("Queen")
            .assertIsDisplayed()
    }

    @Test
    fun conflicting_square_without_queen_shows_no_queen() {
        setContent(hasQueen = false, isConflicting = true)

        composeTestRule
            .onNodeWithContentDescription("Queen")
            .assertDoesNotExist()
    }

    // -- Dark vs Light square --

    @Test
    fun dark_square_with_queen_shows_queen() {
        setContent(isDarkSquare = true, hasQueen = true, showQueen = true)

        composeTestRule
            .onNodeWithContentDescription("Queen")
            .assertIsDisplayed()
    }

    @Test
    fun light_square_with_queen_shows_queen() {
        setContent(isDarkSquare = false, hasQueen = true, showQueen = true)

        composeTestRule
            .onNodeWithContentDescription("Queen")
            .assertIsDisplayed()
    }
}
