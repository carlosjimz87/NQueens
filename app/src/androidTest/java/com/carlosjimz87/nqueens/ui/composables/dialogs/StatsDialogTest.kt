package com.carlosjimz87.nqueens.ui.composables.dialogs

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.carlosjimz87.nqueens.data.model.Leaderboards
import com.carlosjimz87.nqueens.data.model.ScoreEntry
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI tests for StatsDialog validating leaderboard rendering and close interaction.
 */
class StatsDialogTest {

 @get:Rule
 val rule = createComposeRule()

 @Test
 fun `leaderboard title and close button visible`() {
 rule.setContent {
 StatsDialog(
 size = 8,
 leaderboards = Leaderboards(emptyList(), emptyList()),
 onClose = {}
 )
 }

 rule.onNodeWithText("Leaderboard", substring = true).assertIsDisplayed()
 rule.onNodeWithText("Close", substring = true).assertIsDisplayed()
 }

 @Test
 fun `close button triggers callback`() {
 var closed = false

 rule.setContent {
 StatsDialog(
 size = 8,
 leaderboards = Leaderboards(emptyList(), emptyList()),
 onClose = { closed = true }
 )
 }

 rule.onNodeWithText("Close", substring = true).performClick()
 assert(closed)
 }

 @Test
 fun `leaderboard rows render`() {
 val rows = listOf(
 ScoreEntry(size = 8, moves = 8, elapsedMillis = 5000)
 )

 rule.setContent {
 StatsDialog(
 size = 8,
 leaderboards = Leaderboards(rows, emptyList()),
 onClose = {}
 )
 }

 rule.onNodeWithText("8", substring = true).assertIsDisplayed()
 }
}
