package com.carlosjimz87.nqueens.integration

import com.carlosjimz87.nqueens.common.MainDispatcherRule
import com.carlosjimz87.nqueens.board.BoardIntent
import com.carlosjimz87.nqueens.board.Cell
import com.carlosjimz87.nqueens.board.BoardViewModel
import com.carlosjimz87.nqueens.timer.GameTimer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.context.GlobalContext.get

/**
 * Integration test verifying the contract between BoardViewModel and GameTimer.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GameTimerIntegrationTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @Before
    fun setUp() {
        startKoin { modules(appModule) }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    private fun vm(): BoardViewModel = get().get()

    private fun timer(): GameTimer = get().get()

    @Test
    fun `timer starts when first queen is placed`() = runTest {

        val vm = vm()
        val timer = timer()

        vm.dispatch(BoardIntent.ClickCell(Cell(0,0)))

        advanceUntilIdle()

        assertTrue(timer.isRunning())
    }
}
