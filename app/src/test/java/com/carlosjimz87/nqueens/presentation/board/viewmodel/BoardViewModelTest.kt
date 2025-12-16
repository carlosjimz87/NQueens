package com.carlosjimz87.nqueens.presentation.board.viewmodel

import com.carlosjimz87.nqueens.MainDispatcherRule
import com.carlosjimz87.nqueens.di.testModule
import com.carlosjimz87.nqueens.presentation.audio.model.Sound
import com.carlosjimz87.nqueens.presentation.board.effect.BoardEffect
import com.carlosjimz87.nqueens.presentation.board.intent.BoardIntent
import com.carlosjimz87.nqueens.presentation.board.state.BoardPhase
import com.carlosjimz87.rules.model.BoardError
import com.carlosjimz87.rules.model.Cell
import com.carlosjimz87.rules.model.GameStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.context.GlobalContext.stopKoin
import org.koin.java.KoinJavaComponent.getKoin

/**
 * Test suite for the [BoardViewModel].
 *
 * This class contains unit tests that verify the behavior of the [BoardViewModel],
 * ensuring that its state management, event handling, and game logic are correct.
 * It uses a test-specific Koin module [testModule] for dependency injection and
 * [MainDispatcherRule] to manage coroutine dispatchers for testing.
 *
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BoardViewModelTest {

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

    private fun <T> TestScope.awaitItem(
        flow: Flow<T>,
        timeoutMs: Long = 1_000,
        predicate: (T) -> Boolean
    ) = async {
        withTimeout(timeoutMs) { flow.first(predicate) }
    }.also { runCurrent() }

    private suspend fun <T> assertNoItem(
        flow: Flow<T>,
        timeoutMs: Long = 150,
        predicate: (T) -> Boolean
    ) {
        val found = try {
            withTimeout(timeoutMs) { flow.first(predicate) }
            true
        } catch (_: Exception) {
            false
        }
        assertTrue("Unexpected item emitted", !found)
    }

    private fun TestScope.awaitSound(vm: BoardViewModel, sound: Sound, timeoutMs: Long = 1_000) =
        awaitItem(vm.effects, timeoutMs) { it is BoardEffect.PlaySound && it.sound == sound }

    private suspend fun assertNoSound(
        vm: BoardViewModel,
        sound: Sound,
        timeoutMs: Long = 150
    ) =
        assertNoItem(vm.effects, timeoutMs) { it is BoardEffect.PlaySound && it.sound == sound }

    @Test
    fun `init with valid initial size sets boardSize and ends not loading`() = runTest {
        val vm = getVm()
        advanceUntilIdle()

        val s = vm.state.value
        assertEquals(8, s.boardSize)
        assertFalse(s.isLoading)
        assertNull(s.error)
    }

    @Test
    fun `SetBoardSize valid updates boardSize ends not loading and clears queens`() = runTest {
        val vm = getVm()
        advanceUntilIdle()

        // add some queens before size change
        vm.dispatch(BoardIntent.ClickCell(Cell(row = 0, col = 0)))
        vm.dispatch(BoardIntent.ClickCell(Cell(row = 1, col = 1)))
        advanceUntilIdle()
        assertEquals(2, vm.state.value.queens.size)

        vm.dispatch(BoardIntent.SetBoardSize(6))
        advanceUntilIdle()

        val s = vm.state.value
        assertEquals(6, s.boardSize)
        assertFalse(s.isLoading)
        assertNull(s.error)
        assertTrue(s.queens.isEmpty())
    }

    @Test
    fun `SetBoardSize invalid small sets error and keeps last valid boardSize and queens`() =
        runTest {
            val vm = getVm()
            advanceUntilIdle()

            val queenCell = Cell(row = 0, col = 0)
            vm.dispatch(BoardIntent.ClickCell(queenCell))
            advanceUntilIdle()

            assertEquals(setOf(queenCell), vm.state.value.queens)

            vm.dispatch(BoardIntent.SetBoardSize(2))
            advanceUntilIdle()

            val s = vm.state.value
            assertEquals(8, s.boardSize) // keeps previous
            assertEquals(setOf(queenCell), s.queens) // keeps previous
            assertFalse(s.isLoading)
            assertEquals(BoardError.SizeTooSmall, s.error)
        }

    @Test
    fun `SetBoardSize invalid big sets error and keeps last valid boardSize`() = runTest {
        val vm = getVm()
        advanceUntilIdle()

        vm.dispatch(BoardIntent.SetBoardSize(22))
        advanceUntilIdle()

        val s = vm.state.value
        assertEquals(8, s.boardSize)
        assertFalse(s.isLoading)
        assertEquals(BoardError.SizeTooBig, s.error)
    }

    @Test
    fun `SetBoardSize same size does nothing`() = runTest {
        val vm = getVm()
        advanceUntilIdle()

        val queenCell = Cell(row = 0, col = 0)
        vm.dispatch(BoardIntent.ClickCell(queenCell))
        advanceUntilIdle()

        val before = vm.state.value

        vm.dispatch(BoardIntent.SetBoardSize(8))
        advanceUntilIdle()

        val after = vm.state.value
        assertEquals(before.boardSize, after.boardSize)
        assertEquals(before.error, after.error)
        assertEquals(before.isLoading, after.isLoading)
        assertEquals(before.queens, after.queens)
        assertEquals(before.conflicts, after.conflicts)
    }

    @Test
    fun `ClickCell adds queen when cell is empty`() = runTest {
        val vm = getVm()
        advanceUntilIdle()

        val cell = Cell(row = 3, col = 4)
        vm.dispatch(BoardIntent.ClickCell(cell))
        advanceUntilIdle()

        val s = vm.state.value
        assertTrue(cell in s.queens)
        assertEquals(1, s.queens.size)
    }

    @Test
    fun `ClickCell removes queen when cell already has queen`() = runTest {
        val vm = getVm()
        advanceUntilIdle()

        val cell = Cell(row = 3, col = 4)

        vm.dispatch(BoardIntent.ClickCell(cell)) // add
        vm.dispatch(BoardIntent.ClickCell(cell)) // remove
        advanceUntilIdle()

        assertTrue(vm.state.value.queens.isEmpty())
    }

    @Test
    fun `ClickCell on empty cell emits PlaySound QUEEN_PLACED`() = runTest {
        val vm = getVm()
        advanceUntilIdle()

        val awaited = awaitSound(vm, Sound.QUEEN_PLACED)

        val cell = Cell(0, 0)
        vm.dispatch(BoardIntent.ClickCell(cell))
        advanceUntilIdle()

        assertTrue(cell in vm.state.value.queens)
        awaited.await()
    }

    @Test
    fun `ClickCell on occupied cell emits PlaySound QUEEN_REMOVED and not QUEEN_PLACED`() = runTest {
        val vm = getVm()
        advanceUntilIdle()

        val cell = Cell(0, 0)
        vm.dispatch(BoardIntent.ClickCell(cell)) // place
        advanceUntilIdle()

        val awaitedRemoved = awaitSound(vm, Sound.QUEEN_REMOVED)

        vm.dispatch(BoardIntent.ClickCell(cell)) // remove
        advanceUntilIdle()

        assertTrue(vm.state.value.queens.isEmpty())
        awaitedRemoved.await()
        assertNoSound(vm, Sound.QUEEN_PLACED)
    }

    @Test
    fun `init sets NotStarted and elapsed is zero`() = runTest {
        val vm = getVm()
        advanceUntilIdle()

        val status = vm.state.value.gameState?.status
        assertTrue(status is GameStatus.NotStarted)
        status as GameStatus.NotStarted
        assertEquals(8, status.size)

        assertEquals(0L, vm.state.value.elapsedMillis)
    }

    @Test
    fun `placing first queen moves game to InProgress`() = runTest {
        val vm = getVm()
        advanceUntilIdle()

        vm.dispatch(BoardIntent.ClickCell(Cell(row = 0, col = 0)))
        advanceUntilIdle()

        val status = vm.state.value.gameState?.status
        assertTrue(status is GameStatus.InProgress)
        status as GameStatus.InProgress

        assertEquals(8, status.size)
        assertEquals(1, status.queensPlaced)
    }

    @Test
    fun `removing all queens returns game to NotStarted`() = runTest {
        val vm = getVm()
        advanceUntilIdle()

        val cell = Cell(row = 0, col = 0)
        vm.dispatch(BoardIntent.ClickCell(cell)) // place
        vm.dispatch(BoardIntent.ClickCell(cell)) // remove
        advanceUntilIdle()

        val status = vm.state.value.gameState?.status
        assertTrue(status is GameStatus.NotStarted)
        status as GameStatus.NotStarted
        assertEquals(8, status.size)

        // con timer real puede ser 0 o muy cercano a 0; si tu GameTimer fake resetea a 0, esto pasa fijo
        assertEquals(0L, vm.state.value.elapsedMillis)
    }

    @Test
    fun `changing to a new valid size resets queens and sets NotStarted`() = runTest {
        val vm = getVm()
        advanceUntilIdle()

        vm.dispatch(BoardIntent.ClickCell(Cell(row = 0, col = 0)))
        vm.dispatch(BoardIntent.ClickCell(Cell(row = 1, col = 1)))
        advanceUntilIdle()

        vm.dispatch(BoardIntent.SetBoardSize(6))
        advanceUntilIdle()

        val s = vm.state.value
        assertEquals(6, s.boardSize)
        assertTrue(s.queens.isEmpty())
        assertEquals(0L, s.elapsedMillis)

        val status = s.gameState?.status
        assertTrue(status is GameStatus.NotStarted)
        status as GameStatus.NotStarted
        assertEquals(6, status.size)
    }

    @Test
    fun `placing N queens with zero conflicts moves game to Solved and triggers win phase`() =
        runTest {
            val vm = getVm()
            advanceUntilIdle()

            vm.dispatch(BoardIntent.SetBoardSize(4))
            advanceUntilIdle()

            vm.dispatch(BoardIntent.ClickCell(Cell(row = 0, col = 1)))
            vm.dispatch(BoardIntent.ClickCell(Cell(row = 1, col = 3)))
            vm.dispatch(BoardIntent.ClickCell(Cell(row = 2, col = 0)))
            vm.dispatch(BoardIntent.ClickCell(Cell(row = 3, col = 2)))
            advanceUntilIdle()

            val s = vm.state.value
            val status = s.gameState?.status
            assertTrue(status is GameStatus.Solved)
            status as GameStatus.Solved
            assertEquals(4, status.size)
            assertTrue(status.moves >= 4)

            // tu VM pone WinAnimating al resolver
            assertEquals(BoardPhase.WinAnimating, s.boardPhase)
        }

    @Test
    fun `ResetGame re-applies size clears latestRank sets phase Normal`() = runTest {
        val vm = getVm()
        advanceUntilIdle()

        // forces a win
        vm.dispatch(BoardIntent.EnterWinAnimation)
        advanceUntilIdle()
        assertEquals(BoardPhase.WinAnimating, vm.state.value.boardPhase)

        vm.dispatch(BoardIntent.ResetGame)
        advanceUntilIdle()

        val s = vm.state.value
        assertEquals(8, s.boardSize)
        assertFalse(s.isLoading)
        assertNull(s.error)
        assertEquals(BoardPhase.Normal, s.boardPhase)
        assertNull(s.latestRank)
        assertEquals(0L, s.elapsedMillis)
        assertTrue(s.queens.isEmpty())
    }

    @Test
    fun `EnterWinAnimation sets boardPhase WinAnimating`() = runTest {
        val vm = getVm()
        advanceUntilIdle()

        assertEquals(BoardPhase.Normal, vm.state.value.boardPhase)

        vm.dispatch(BoardIntent.EnterWinAnimation)
        advanceUntilIdle()

        assertEquals(BoardPhase.WinAnimating, vm.state.value.boardPhase)
    }

    @Test
    fun `WinAnimationFinished sets boardPhase WinFrozen`() = runTest {
        val vm = getVm()
        advanceUntilIdle()

        vm.dispatch(BoardIntent.EnterWinAnimation)
        advanceUntilIdle()
        assertEquals(BoardPhase.WinAnimating, vm.state.value.boardPhase)

        vm.dispatch(BoardIntent.WinAnimationFinished)
        advanceUntilIdle()

        assertEquals(BoardPhase.WinFrozen, vm.state.value.boardPhase)
    }
}