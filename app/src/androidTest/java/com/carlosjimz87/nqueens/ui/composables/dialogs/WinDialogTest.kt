package com.carlosjimz87.nqueens.ui.composables.dialogs

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.carlosjimz87.rules.model.GameStatus
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI tests verifying WinDialog actions and visible content.
 */
class WinDialogTest {

 @get:Rule
 val rule = createComposeRule()

 @Test
 fun `dialog renders solved buttons`() {

 rule.setContent {
 WinDialog(
 solved = GameStatus.Solved(8, 8),
 elapsedMillis = 1000,
 rankByTime = 1,
 rankByMoves = 1,
 onPlayAgain = {},
 onNextLevel = {},
 onShowStats = {}
 )
 }

 rule.onNodeWithText("Play Again", substring = true).assertIsDisplayed()
 }

 @Test
 fun `play again triggers callback`() {
 var invoked = false

 rule.setContent {
 WinDialog(
 solved = GameStatus.Solved(8, 8),
 elapsedMillis = 1000,
 rankByTime = 1,
 rankByMoves = 1,
 onPlayAgain = { invoked = true },
 onNextLevel = {},
 onShowStats = {}
 )
 }

 rule.onNodeWithText("Play", substring = true).performClick()
 assert(invoked)
 }
}
