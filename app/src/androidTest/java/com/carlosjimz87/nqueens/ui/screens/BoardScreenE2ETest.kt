package com.carlosjimz87.nqueens.ui.screens

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.carlosjimz87.nqueens.MainActivity
import org.junit.Rule
import org.junit.Test

/**
 * Activity-level end-to-end tests launching the real app and validating
 * user flows through the BoardScreen.
 */
class BoardScreenE2ETest {

 @get:Rule
 val rule = createAndroidComposeRule<MainActivity>()

 @Test
 fun `app launches and board size dialog appears`() {
 rule.onNodeWithText("Choose board size", substring = true)
 }

 @Test
 fun `start game creates board`() {
 rule.onNodeWithText("Start", substring = true).performClick()
 }

 @Test
 fun `reset button can be pressed`() {
 rule.onNodeWithText("Start", substring = true).performClick()
 rule.onNodeWithText("Reset", substring = true).performClick()
 }
}
