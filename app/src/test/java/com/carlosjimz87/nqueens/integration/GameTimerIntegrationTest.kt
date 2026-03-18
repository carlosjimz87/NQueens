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
 * Integration tests verifying the contract between [BoardViewModel] and [GameTimer].
 * These tests check that the ViewModel correctly drives timer start/stop/reset
 * at the right game lifecycle moments.
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

    private fun getVm(): BoardViewModel = getKoin().get()
    private fun getTimer(): FakeGameTimer = getKoin().get<GameTimer>() as FakeGameTimer

    @Test
    fun `timer start is called exactly once when the first queen is placed`() = runTest {
        val vm = getVm()
        val timer = getTimer()
        advanceUntilIdle()

        assertEquals(0, timer.startCalled)

        vm.dispatch(BoardIntent.ClickCell(Cell(row = 0, col = 0)))
        advanceUntilIdle()

        assertEquals(1, timer.startCalled)
    }

    @Test
    fun `timer start is NOT called again on subsequent queen placements`() = runTest {
        val vm = getVm()
        val timer = getTimer()
        advanceUntilIdle()

        vm.dispatch(BoardIntent.ClickCell(Cell(row = 0, col = 0))) // first queen -> starts timer
        vm.dispatch(BoardIntent.ClickCell(Cell(row = 2, col = 4))) // second queen -> no start
        vm.dispatch(BoardIntent.ClickCell(Cell(row = 4, col = 1))) // third queen -> no start
        advanceUntilIdle()

        assertEquals(1, timer.startCalled)
    }

    @Test
    fun `timer reset is called on ResetGame and elapsed is set back to zero`() = runTest {
        val vm = getVm()
        val timer = getTimer()
        advanceUntilIdle()

        val resetCallsAfterInit = timer.resetCalled

        // Simulate some elapsed time manually through FakeGameTimer
        timer.setElapsed(5_000L)
        advanceUntilIdle()
        assertEquals(5_000L, vm.state.value.elapsedMillis)

        vm.dispatch(BoardIntent.ResetGame)
        advanceUntilIdle()

        assertEquals(resetCallsAfterInit + 1, timer.resetCalled)
        assertEquals(0L, vm.state.value.elapsedMillis)
    }
}
