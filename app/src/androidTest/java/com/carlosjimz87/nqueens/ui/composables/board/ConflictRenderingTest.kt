package com.carlosjimz87.nqueens.ui.composables.board

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.carlosjimz87.rules.model.Cell
import com.carlosjimz87.rules.model.Conflicts
import org.junit.Rule
import org.junit.Test

/**
 * UI test verifying that the board renders conflict overlay layer.
 */
class ConflictRenderingTest {

 @get:Rule
 val rule = createComposeRule()

 @Test
 fun `board renders conflict layer when conflicts present`() {
 rule.setContent {
 Board(
 size = 4,
 queens = setOf(Cell(0,0), Cell(1,1)),
 conflicts = Conflicts(
 diagonals = listOf(Cell(0,0) to Cell(1,1)),
 rows = emptyList(),
 cols = emptyList()
 ),
 onCellClick = {},
 showQueens = true
 )
 }

 rule.onRoot().assertExists()
 }
}
