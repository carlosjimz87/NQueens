package com.carlosjimz87.nqueens.integration

import com.carlosjimz87.nqueens.MainDispatcherRule
import com.carlosjimz87.nqueens.di.testModule
import com.carlosjimz87.nqueens.presentation.board.intent.BoardIntent
import com.carlosjimz87.nqueens.presentation.board.viewmodel.BoardViewModel
import com.carlosjimz87.nqueens.presentation.timer.FakeGameTimer
import com.carlosjimz87.nqueens.presentation.timer.GameTimer
import com.carlosjimz87.rules.model.Cell
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.context.GlobalContext.stopKoin
import org.koin.java.KoinJavaComponent.getKoin

/**
 * Integration tests for the ViewModel-to-Timer contract.
 *
 * Uses Koin [testModule] with [FakeGameTimer] to verify that [BoardViewModel]
 * interacts with the timer correctly during game operations:
 * - Timer starts exactly once on first queen placement
 * - Timer does not restart on subsequent placements
 * - Timer resets when the game is reset
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GameTimerIntegrationTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Before
    fun setUp() {
        startKoin { modules(testModule) }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    private fun fakeTimer(): FakeGameTimer = getKoin().get<GameTimer>() as FakeGameTimer
    private fun viewModel(): BoardViewModel = getKoin().get()

    // -- Timer start behavior --

    @Test
    fun `timer start is called exactly once on first queen placement`() = runTest {
        val vm = viewModel()
        val timer = fakeTimer()
        advanceUntilIdle() // let initial SetBoardSize(8) resolve

        assertEquals(0, timer.startCalled)

        vm.dispatch(BoardIntent.ClickCell(Cell(0, 0)))
        advanceUntilIdle()

        assertEquals(1, timer.startCalled)
    }

    @Test
    fun `timer is not restarted on subsequent queen placements`() = runTest {
        val vm = viewModel()
        val timer = fakeTimer()
        advanceUntilIdle()

        // Place first queen -- triggers timer.start()
        vm.dispatch(BoardIntent.ClickCell(Cell(0, 0)))
        advanceUntilIdle()
        assertEquals(1, timer.startCalled)

        // Place second queen -- should NOT call timer.start() again
        vm.dispatch(BoardIntent.ClickCell(Cell(1, 2)))
        advanceUntilIdle()
        assertEquals(1, timer.startCalled)

        // Place third queen -- still no restart
        vm.dispatch(BoardIntent.ClickCell(Cell(2, 4)))
        advanceUntilIdle()
        assertEquals(1, timer.startCalled)
    }

    // -- Timer reset behavior --

    @Test
    fun `timer reset is called on ResetGame and elapsed returns to zero`() = runTest {
        val vm = viewModel()
        val timer = fakeTimer()
        advanceUntilIdle()

        // Place a queen to start the timer
        vm.dispatch(BoardIntent.ClickCell(Cell(0, 0)))
        advanceUntilIdle()
        assertEquals(1, timer.startCalled)

        // Simulate elapsed time
        timer.setElapsed(5000L)
        advanceUntilIdle()
        assertEquals(5000L, vm.state.value.elapsedMillis)

        // Reset game
        vm.dispatch(BoardIntent.ResetGame)
        advanceUntilIdle()

        // resetCalled should include: 1 from initial SetBoardSize(8) + 1 from ResetGame
        // The init block dispatches SetBoardSize(8), which calls resetBoard() -> timer.reset()
        // Then ResetGame calls resetBoard() -> timer.reset() again
        assertEquals(0L, timer.elapsedMillis.value)
        assertEquals(0L, vm.state.value.elapsedMillis)
        // Verify reset was called (at least the one from ResetGame)
        assertTrue(timer.resetCalled >= 2)
    }

    // -- Timer stop not called without solve --

    @Test
    fun `timer stop is not called for regular queen placements`() = runTest {
        val vm = viewModel()
        val timer = fakeTimer()
        advanceUntilIdle()

        vm.dispatch(BoardIntent.ClickCell(Cell(0, 0)))
        advanceUntilIdle()

        vm.dispatch(BoardIntent.ClickCell(Cell(1, 2)))
        advanceUntilIdle()

        assertEquals(0, timer.stopCalled)
    }

    private fun assertTrue(condition: Boolean) {
        org.junit.Assert.assertTrue(condition)
    }
}
