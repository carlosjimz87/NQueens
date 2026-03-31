package com.carlosjimz87.nqueens.ui.composables.hud

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI tests validating GameHud visible elements.
 */
class GameHudTest {

 @get:Rule
 val rule = createComposeRule()

 @Test
 fun `hud renders timer text`() {
 rule.setContent {
 GameHud(
 elapsedMillis = 2000,
 onChange = {},
 onReset = {},
 onStats = {}
 )
 }

 rule.onNodeWithText("00:", substring = true).assertIsDisplayed()
 }
}
