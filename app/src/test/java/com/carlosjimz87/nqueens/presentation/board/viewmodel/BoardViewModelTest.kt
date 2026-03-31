package com.carlosjimz87.nqueens.presentation.board.viewmodel

import com.carlosjimz87.nqueens.board.BoardIntent
import com.carlosjimz87.nqueens.board.BoardViewModel
import com.carlosjimz87.nqueens.board.Cell
import com.carlosjimz87.nqueens.common.MainDispatcherRule
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
 * Unit tests for BoardViewModel verifying board state transitions.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BoardViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @Before
    fun setup() {
        startKoin { modules(appModule) }
    }

    @After
    fun teardown() {
        stopKoin()
    }

    private fun vm(): BoardViewModel = get().get()

    @Test
    fun `placing queen updates board state`() = runTest {

        val vm = vm()

        vm.dispatch(BoardIntent.ClickCell(Cell(0,0)))
        advanceUntilIdle()

        assertTrue(vm.state.value.queens.contains(Cell(0,0)))
    }
}
