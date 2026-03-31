package com.carlosjimz87.nqueens.ui.composables.board

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodes
import androidx.compose.ui.test.performClick
import com.carlosjimz87.rules.model.Cell
import org.junit.Rule
import org.junit.Test

/**
 * Compose interaction tests validating board clicks propagate correctly.
 */
class BoardGridInteractionTest {

 @get:Rule
 val rule = createComposeRule()

 @Test
 fun `clicking square invokes callback`() {
 var clickedCell: Cell? = null

 rule.setContent {
 BoardAdaptative(
 boardSize = 4,
 isLoading = false,
 queens = emptyList(),
 conflicts = emptyList(),
 gameStatus = null,
 elapsedMillis = 0,
 allowClicks = true,
 showQueens = true,
 onCellClick = { clickedCell = it },
 onChange = {},
 onReset = {},
 onStats = {}
 )
 }

 rule.onAllNodes(hasClickAction())[0].performClick()
 assert(clickedCell != null)
 }
}
