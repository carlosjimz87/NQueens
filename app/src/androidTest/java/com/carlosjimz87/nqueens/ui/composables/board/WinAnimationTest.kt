package com.carlosjimz87.nqueens.ui.composables.board

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.carlosjimz87.rules.model.Cell
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI test validating that the win animation layer can render
 * without throwing errors when enabled.
 */
class WinAnimationTest {

 @get:Rule
 val rule = createComposeRule()

 @Test
 fun `win animation renders with queens`() {

 val queens = listOf(
 Cell(0,0), Cell(1,2), Cell(2,4), Cell(3,1)
 )

 rule.setContent {
 WinAnimation(
 token = 1,
 size = 4,
 queens = queens,
 enabled = true,
 onFinished = {}
 )
 }

 rule.onRoot().assertExists()
 }
}
