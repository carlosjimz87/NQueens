package com.carlosjimz87.nqueens.presentation.board.viewmodel

import com.carlosjimz87.nqueens.MainDispatcherRule
import com.carlosjimz87.nqueens.domain.error.BoardError
import com.carlosjimz87.nqueens.domain.model.Cell
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
    fun `onSizeChanged with valid new size updates boardSize goes Idle and clears queens`() = runTest {
        val vm = BoardViewModel(initialSize = 8)
        advanceUntilIdle()

        // add some queens before size change
        vm.onCellClicked(Cell(row = 0, col = 0))
        vm.onCellClicked(Cell(row = 1, col = 1))
        assertEquals(2, vm.queens.value.size)

        vm.onSizeChanged(6)

        advanceUntilIdle()

        assertEquals(6, vm.boardSize.value)
        assertTrue(vm.uiState.value is UiState.Idle)
        assertTrue(vm.queens.value.isEmpty())
    }

    @Test
    fun `onSizeChanged with invalid size emits BoardInvalid(small) and keeps last valid boardSize and queens`() = runTest {
        val vm = BoardViewModel(initialSize = 8)
        advanceUntilIdle()

        val queenCell = Cell(row = 0, col = 0)
        vm.onCellClicked(queenCell)
        assertEquals(setOf(queenCell), vm.queens.value)

        vm.onSizeChanged(2)

        advanceUntilIdle()

        assertEquals(8, vm.boardSize.value)

        assertEquals(setOf(queenCell), vm.queens.value)

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

        val queenCell = Cell(row = 0, col = 0)
        vm.onCellClicked(queenCell)
        assertEquals(setOf(queenCell), vm.queens.value)

        val initialBoardSize = vm.boardSize.value
        val initialUiState = vm.uiState.value

        vm.onSizeChanged(8)

        advanceUntilIdle()

        assertEquals(initialBoardSize, vm.boardSize.value)
        assertEquals(initialUiState, vm.uiState.value)
        assertEquals(setOf(queenCell), vm.queens.value)
    }

    @Test
    fun `onCellClicked adds queen when cell is empty`() = runTest {
        val vm = BoardViewModel(initialSize = 8)
        advanceUntilIdle()

        val cell = Cell(row = 3, col = 4)

        vm.onCellClicked(cell)

        assertTrue(cell in vm.queens.value)
        assertEquals(1, vm.queens.value.size)
    }

    @Test
    fun `onCellClicked removes queen when cell already has queen`() = runTest {
        val vm = BoardViewModel(initialSize = 8)
        advanceUntilIdle()

        val cell = Cell(row = 3, col = 4)

        vm.onCellClicked(cell)  // add
        vm.onCellClicked(cell)  // remove

        assertTrue(vm.queens.value.isEmpty())
    }
}