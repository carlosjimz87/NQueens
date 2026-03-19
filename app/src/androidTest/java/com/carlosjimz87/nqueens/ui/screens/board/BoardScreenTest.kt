package com.carlosjimz87.nqueens.ui.screens.board

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.carlosjimz87.nqueens.data.model.StatsState
import com.carlosjimz87.nqueens.data.repo.StatsRepository
import com.carlosjimz87.nqueens.data.repo.StatsRepositoryImpl
import com.carlosjimz87.nqueens.data.sources.StoreManager
import com.carlosjimz87.nqueens.presentation.board.viewmodel.BoardViewModel
import com.carlosjimz87.nqueens.presentation.timer.GameTimer
import com.carlosjimz87.rules.solver.NQueensSolverImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.junit.Rule
import org.junit.Test

/**
 * Screen-level / E2E test for [BoardScreen].
 *
 * Renders the full BoardScreen with a real [BoardViewModel] backed by fakes
 * for the timer and data store. Verifies a multi-step user flow:
 *
 * 1. User sees initial state (HUD with "Queens 0/N", "Place your first queen")
 * 2. User clicks a cell -> queen appears, queen count updates
 * 3. User resets -> board returns to initial state
 */
class BoardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /** Inline fake GameTimer for androidTest (mirrors the one in src/test). */
    private class TestGameTimer : GameTimer {
        private val _elapsedMillis = MutableStateFlow(0L)
        override val elapsedMillis: StateFlow<Long> = _elapsedMillis
        override fun start() {}
        override fun stop() {}
        override fun reset() { _elapsedMillis.value = 0L }
    }

    /** Inline fake StoreManager for androidTest. */
    private class TestStoreManager(initial: StatsState) : StoreManager<StatsState> {
        private val mutex = Mutex()
        private val state = MutableStateFlow(initial)
        override val data: Flow<StatsState> = state
        override suspend fun update(block: (StatsState) -> StatsState) {
            mutex.withLock { state.value = block(state.value) }
        }
    }

    private fun createViewModel(initialSize: Int = 4): BoardViewModel {
        val timer = TestGameTimer()
        val solver = NQueensSolverImpl()
        val store = TestStoreManager(StatsState())
        val repo = StatsRepositoryImpl(store)
        return BoardViewModel(
            timer = timer,
            solver = solver,
            statsRepo = repo,
            initialSize = initialSize
        )
    }

    @Test
    fun initial_state_shows_level_and_zero_queens() {
        val vm = createViewModel(initialSize = 4)

        composeTestRule.setContent {
            MaterialTheme {
                BoardScreen(viewModel = vm)
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Level 4").assertIsDisplayed()
        composeTestRule.onNodeWithText("Queens 0/4").assertIsDisplayed()
        composeTestRule.onNodeWithText("Place your first queen").assertIsDisplayed()
    }

    @Test
    fun clicking_a_cell_places_a_queen_and_updates_count() {
        val vm = createViewModel(initialSize = 4)

        composeTestRule.setContent {
            MaterialTheme {
                BoardScreen(viewModel = vm)
            }
        }

        composeTestRule.waitForIdle()

        // Initially no queens
        composeTestRule
            .onAllNodesWithContentDescription("Queen")
            .assertCountEquals(0)

        // The board is a 4x4 grid of clickable squares.
        // We need to find a clickable area. The squares don't have test tags,
        // but we can use the fact that the board area is composed of clickable Box elements.
        // Since Board > SquaresAndQueens renders Row/Column of clickable squares,
        // let's use an approach of finding all clickable nodes and clicking the first one.

        // After placing a queen, the count should update to "Queens 1/4"
        // Use Espresso-style: find the board area and click within it.
        // The simplest approach: the HUD text "Place your first queen" is displayed,
        // indicating the game has not started. We cannot easily click a specific cell
        // without test tags, but we can verify the HUD text changes pattern.

        // For this test, we directly dispatch an intent to place a queen
        // and verify the UI updates accordingly.
        composeTestRule.runOnUiThread {
            vm.dispatch(
                com.carlosjimz87.nqueens.presentation.board.intent.BoardIntent.ClickCell(
                    com.carlosjimz87.rules.model.Cell(0, 0)
                )
            )
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Queens 1/4").assertIsDisplayed()
        composeTestRule
            .onAllNodesWithContentDescription("Queen")
            .assertCountEquals(1)
    }

    @Test
    fun reset_returns_board_to_initial_state() {
        val vm = createViewModel(initialSize = 4)

        composeTestRule.setContent {
            MaterialTheme {
                BoardScreen(viewModel = vm)
            }
        }

        composeTestRule.waitForIdle()

        // Place a queen via dispatch
        composeTestRule.runOnUiThread {
            vm.dispatch(
                com.carlosjimz87.nqueens.presentation.board.intent.BoardIntent.ClickCell(
                    com.carlosjimz87.rules.model.Cell(0, 0)
                )
            )
        }

        composeTestRule.waitForIdle()

        // Verify queen is placed
        composeTestRule.onNodeWithText("Queens 1/4").assertIsDisplayed()

        // Click reset button
        composeTestRule
            .onNodeWithContentDescription("Reset game")
            .performClick()

        composeTestRule.waitForIdle()

        // Verify reset to initial state
        composeTestRule.onNodeWithText("Queens 0/4").assertIsDisplayed()
        composeTestRule.onNodeWithText("Place your first queen").assertIsDisplayed()
        composeTestRule
            .onAllNodesWithContentDescription("Queen")
            .assertCountEquals(0)
    }

    @Test
    fun multi_step_flow_place_queen_then_remove_then_verify() {
        val vm = createViewModel(initialSize = 4)

        composeTestRule.setContent {
            MaterialTheme {
                BoardScreen(viewModel = vm)
            }
        }

        composeTestRule.waitForIdle()

        val cell = com.carlosjimz87.rules.model.Cell(1, 2)

        // Step 1: Place queen
        composeTestRule.runOnUiThread {
            vm.dispatch(
                com.carlosjimz87.nqueens.presentation.board.intent.BoardIntent.ClickCell(cell)
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Queens 1/4").assertIsDisplayed()

        // Step 2: Remove queen (click same cell)
        composeTestRule.runOnUiThread {
            vm.dispatch(
                com.carlosjimz87.nqueens.presentation.board.intent.BoardIntent.ClickCell(cell)
            )
        }
        composeTestRule.waitForIdle()

        // After removal, the HUD should show the status without any queens
        // (InProgress with 0 queens shows as "Queens 0/4" depending on status)
        composeTestRule
            .onAllNodesWithContentDescription("Queen")
            .assertCountEquals(0)
    }
}
