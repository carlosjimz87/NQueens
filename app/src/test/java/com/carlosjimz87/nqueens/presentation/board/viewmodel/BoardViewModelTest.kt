package com.carlosjimz87.nqueens.presentation.board.viewmodel

import com.carlosjimz87.nqueens.MainDispatcherRule
import com.carlosjimz87.nqueens.domain.error.BoardError
import com.carlosjimz87.nqueens.domain.model.Cell
import com.carlosjimz87.nqueens.domain.model.GameStatus
import com.carlosjimz87.nqueens.presentation.board.event.UiEvent
import com.carlosjimz87.nqueens.presentation.board.state.UiState
import com.carlosjimz87.nqueens.presentation.timer.FakeGameTimer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BoardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `init with valid initial size sets boardSize and ends Idle`() = runTest {
        val vm = BoardViewModel(initialSize = 8, FakeGameTimer())

        advanceUntilIdle()

        assertEquals(8, vm.boardSize.value)
        assertTrue(vm.uiState.value is UiState.Idle)
    }

    @Test
    fun `onSizeChanged with valid new size updates boardSize goes Idle and clears queens`() = runTest {
        val vm = BoardViewModel(initialSize = 8, FakeGameTimer())
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
        val vm = BoardViewModel(initialSize = 8, FakeGameTimer())
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
        val vm = BoardViewModel(initialSize = 8, FakeGameTimer())
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
        val vm = BoardViewModel(initialSize = 8, FakeGameTimer())
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
        val vm = BoardViewModel(initialSize = 8,FakeGameTimer())
        advanceUntilIdle()

        val cell = Cell(row = 3, col = 4)

        vm.onCellClicked(cell)

        assertTrue(cell in vm.queens.value)
        assertEquals(1, vm.queens.value.size)
    }

    @Test
    fun `onCellClicked removes queen when cell already has queen`() = runTest {
        val vm = BoardViewModel(initialSize = 8, FakeGameTimer())
        advanceUntilIdle()

        val cell = Cell(row = 3, col = 4)

        vm.onCellClicked(cell)  // add
        vm.onCellClicked(cell)  // remove

        assertTrue(vm.queens.value.isEmpty())
    }

    @Test
    fun `onCellClicked on empty cell adds queen and emits QueenPlaced`() = runTest {
        val vm = BoardViewModel(initialSize = 8, FakeGameTimer())
        advanceUntilIdle()

        val cell = Cell(row = 0, col = 0)

        val events = mutableListOf<UiEvent>()
        val job = launch {
            vm.events.collect { event ->
                events.add(event)
            }
        }

        vm.onCellClicked(cell)

        advanceUntilIdle()

        assertTrue(cell in vm.queens.value)
        assertTrue(events.any { it is UiEvent.QueenPlaced })

        job.cancel()
    }

    @Test
    fun `onCellClicked on occupied cell removes queen and does not emit new QueenPlaced`() = runTest {
        val vm = BoardViewModel(initialSize = 8,FakeGameTimer())
        advanceUntilIdle()

        val cell = Cell(row = 0, col = 0)

        val events = mutableListOf<UiEvent>()
        val job = launch {
            vm.events.collect { event ->
                events.add(event)
            }
        }

        vm.onCellClicked(cell)
        advanceUntilIdle()
        events.clear()

        vm.onCellClicked(cell)
        advanceUntilIdle()

        assertTrue(vm.queens.value.isEmpty())
        assertTrue(events.none { it is UiEvent.QueenPlaced })

        job.cancel()
    }

    @Test
    fun `init with valid initial size sets NotStarted and elapsed is zero`() = runTest {
        val vm = BoardViewModel(initialSize = 8, FakeGameTimer())

        advanceUntilIdle()

        val status = vm.gameStatus.value
        assertTrue(status is GameStatus.NotStarted)
        status as GameStatus.NotStarted
        assertEquals(8, status.size)

        // Timer starts at zero
        assertEquals(0L, vm.elapsedMillis.value)
    }

    @Test
    fun `placing first queen moves game to InProgress`() = runTest {
        val vm = BoardViewModel(initialSize = 8, FakeGameTimer())
        advanceUntilIdle()

        val cell = Cell(row = 0, col = 0)

        vm.onCellClicked(cell)
        advanceUntilIdle()

        val status = vm.gameStatus.value
        assertTrue(status is GameStatus.InProgress)
        status as GameStatus.InProgress

        assertEquals(8, status.size)
        assertEquals(1, status.queensPlaced)
        // We do not assert elapsedMillis here to avoid flakiness
    }

    @Test
    fun `removing all queens returns game to NotStarted and resets timer`() = runTest {
        val vm = BoardViewModel(initialSize = 8, FakeGameTimer())
        advanceUntilIdle()

        val cell = Cell(row = 0, col = 0)

        // First move: place queen
        vm.onCellClicked(cell)
        advanceUntilIdle()

        // Second move: remove queen
        vm.onCellClicked(cell)
        advanceUntilIdle()

        val status = vm.gameStatus.value
        assertTrue(status is GameStatus.NotStarted)
        status as GameStatus.NotStarted
        assertEquals(8, status.size)

        // After going back to NotStarted, timer should be reset
        assertEquals(0L, vm.elapsedMillis.value)
    }

    @Test
    fun `changing to a new valid size resets queens moves and timer and sets NotStarted`() = runTest {
        val vm = BoardViewModel(initialSize = 8, FakeGameTimer())
        advanceUntilIdle()

        // Place some queens to simulate a game in progress
        vm.onCellClicked(Cell(row = 0, col = 0))
        vm.onCellClicked(Cell(row = 1, col = 1))
        advanceUntilIdle()

        // Change board size
        vm.onSizeChanged(6)
        advanceUntilIdle()

        assertEquals(6, vm.boardSize.value)

        val status = vm.gameStatus.value
        assertTrue(status is GameStatus.NotStarted)
        status as GameStatus.NotStarted
        assertEquals(6, status.size)

        assertTrue(vm.queens.value.isEmpty())
        // Timer must be reset on size change
        assertEquals(0L, vm.elapsedMillis.value)
    }

    @Test
    fun `placing N queens with zero conflicts moves game to Solved`() = runTest {
        val vm = BoardViewModel(initialSize = 4, FakeGameTimer())
        advanceUntilIdle()

        // For now, conflicts are always 0, so any 4 distinct cells will trigger Solved
        vm.onCellClicked(Cell(row = 0, col = 0))
        vm.onCellClicked(Cell(row = 1, col = 1))
        vm.onCellClicked(Cell(row = 2, col = 2))
        vm.onCellClicked(Cell(row = 3, col = 3))

        advanceUntilIdle()

        val status = vm.gameStatus.value
        assertTrue(status is GameStatus.Solved)
        status as GameStatus.Solved

        assertEquals(4, status.size)
        // We just check moves are at least the number of placements
        assertTrue(status.moves >= 4)
    }

    @Test
    fun `first queen starts timer`() = runTest {
        val timer = FakeGameTimer()
        val vm = BoardViewModel(initialSize = 8, timer = timer)

        advanceUntilIdle()

        vm.onCellClicked(Cell(0, 0))

        assertEquals(1, timer.startCalled)
    }
}