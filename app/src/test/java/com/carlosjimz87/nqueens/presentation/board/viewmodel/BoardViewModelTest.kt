package com.carlosjimz87.nqueens.presentation.board.viewmodel

import com.carlosjimz87.nqueens.MainDispatcherRule
import com.carlosjimz87.nqueens.domain.error.BoardError
import com.carlosjimz87.nqueens.presentation.board.state.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BoardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `init with valid initial size sets boardSize and ends Idle`() = runTest {
        val vm = BoardViewModel(initialSize = 8)

        advanceUntilIdle()

        assertEquals(8, vm.boardSize.value)
        assertTrue(vm.uiState.value is UiState.Idle)
    }

    @Test
    fun `onSizeChanged with valid new size updates boardSize and goes Idle`() = runTest {
        val vm = BoardViewModel(initialSize = 8)

        advanceUntilIdle()

        vm.onSizeChanged(6)

        advanceUntilIdle()

        assertEquals(6, vm.boardSize.value)
        assertTrue(vm.uiState.value is UiState.Idle)
    }

    @Test
    fun `onSizeChanged with invalid size emits BoardInvalid(small) and keeps last valid boardSize`() = runTest {
        val vm = BoardViewModel(initialSize = 8)
        advanceUntilIdle()

        vm.onSizeChanged(2)

        advanceUntilIdle()

        assertEquals(8, vm.boardSize.value)

        val state = vm.uiState.value
        assertTrue(state is UiState.BoardInvalid)
        state as UiState.BoardInvalid
        assertEquals(BoardError.SizeTooSmall, state.error)
    }


    @Test
    fun `onSizeChanged with invalid size emits BoardInvalid(big) and keeps last valid boardSize`() = runTest {
        val vm = BoardViewModel(initialSize = 8)
        advanceUntilIdle()

        vm.onSizeChanged(22)

        advanceUntilIdle()

        assertEquals(8, vm.boardSize.value)

        val state = vm.uiState.value
        assertTrue(state is UiState.BoardInvalid)
        state as UiState.BoardInvalid
        assertEquals(BoardError.SizeTooBig, state.error)
    }

    @Test
    fun `onSizeChanged with same size does nothing`() = runTest {
        val vm = BoardViewModel(initialSize = 8)
        advanceUntilIdle()

        val initialBoardSize = vm.boardSize.value
        val initialUiState = vm.uiState.value

        vm.onSizeChanged(8)

        advanceUntilIdle()

        assertEquals(initialBoardSize, vm.boardSize.value)
        assertEquals(initialUiState, vm.uiState.value)
    }
}