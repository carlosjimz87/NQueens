package com.carlosjimz87.nqueens.presentation.board.viewmodel

import com.carlosjimz87.nqueens.MainDispatcherRule
import com.carlosjimz87.nqueens.data.model.LatestRank
import com.carlosjimz87.nqueens.di.testModule
import com.carlosjimz87.nqueens.presentation.audio.model.Sound
import com.carlosjimz87.nqueens.presentation.board.effect.BoardEffect
import com.carlosjimz87.nqueens.presentation.board.intent.BoardIntent
import com.carlosjimz87.rules.model.Cell
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.context.GlobalContext.stopKoin
import org.koin.java.KoinJavaComponent.getKoin

@OptIn(ExperimentalCoroutinesApi::class)
class BoardViewModelExtendedTest {

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
        assertTrue("Unexpected item found in flow", !found)
    }

    private fun TestScope.awaitSound(vm: BoardViewModel, sound: Sound, timeoutMs: Long = 1_000) =
        awaitItem(vm.effects, timeoutMs) { it is BoardEffect.PlaySound && it.sound == sound }

    private suspend fun assertNoSound(vm: BoardViewModel, sound: Sound, timeoutMs: Long = 150) =
        assertNoItem(vm.effects, timeoutMs) { it is BoardEffect.PlaySound && it.sound == sound }

    @Test
    fun `SetBoardSize valid emits BOARD_RESET sound`() = runTest {
        val vm = getVm()
        advanceUntilIdle()

        val awaited = awaitSound(vm, Sound.BOARD_RESET)

        vm.dispatch(BoardIntent.SetBoardSize(6))
        advanceUntilIdle()

        awaited.await()
    }

    @Test
    fun `SetBoardSize too small emits ShowSnackbar with correct key`() = runTest {
        val vm = getVm()
        advanceUntilIdle()

        val awaited = awaitItem(vm.effects, 1_000) {
            it is BoardEffect.ShowSnackbar && it.message == BoardViewModel.INVALID_BOARD_SIZE_KEY
        }

        vm.dispatch(BoardIntent.SetBoardSize(2))
        advanceUntilIdle()

        awaited.await()
    }

    @Test
    fun `SetBoardSize too big emits ShowSnackbar with correct key`() = runTest {
        val vm = getVm()
        advanceUntilIdle()

        val awaited = awaitItem(vm.effects, 1_000) {
            it is BoardEffect.ShowSnackbar && it.message == BoardViewModel.INVALID_BOARD_SIZE_KEY
        }

        vm.dispatch(BoardIntent.SetBoardSize(25))
        advanceUntilIdle()

        awaited.await()
    }

    @Test
    fun `ClickCell emits CONFLICT_DETECTED sound for a queen placed in conflict`() = runTest {
        val vm = getVm()
        advanceUntilIdle()

        // Place a queen at (0,0)
        vm.dispatch(BoardIntent.ClickCell(Cell(0, 0)))
        advanceUntilIdle()

        val awaited = awaitSound(vm, Sound.CONFLICT_DETECTED)

        // Place a queen in the same row (same row = conflict)
        vm.dispatch(BoardIntent.ClickCell(Cell(0, 3)))
        advanceUntilIdle()

        awaited.await()
    }

    @Test
    fun `ClickCell emits QUEEN_PLACED and not CONFLICT_DETECTED for non-conflicting placement`() = runTest {
        val vm = getVm()
        advanceUntilIdle()

        val awaited = awaitSound(vm, Sound.QUEEN_PLACED)

        // First queen has no conflict
        vm.dispatch(BoardIntent.ClickCell(Cell(0, 0)))
        advanceUntilIdle()

        awaited.await()
        assertNoSound(vm, Sound.CONFLICT_DETECTED)
    }

    @Test
    fun `winning emits SOLVED sound`() = runTest {
        val vm = getVm()
        advanceUntilIdle()

        vm.dispatch(BoardIntent.SetBoardSize(4))
        advanceUntilIdle()

        val awaited = awaitSound(vm, Sound.SOLVED)

        vm.dispatch(BoardIntent.ClickCell(Cell(row = 0, col = 1)))
        vm.dispatch(BoardIntent.ClickCell(Cell(row = 1, col = 3)))
        vm.dispatch(BoardIntent.ClickCell(Cell(row = 2, col = 0)))
        vm.dispatch(BoardIntent.ClickCell(Cell(row = 3, col = 2)))
        advanceUntilIdle()

        awaited.await()
    }

    @Test
    fun `winning sets latestRank to non-null value in state`() = runTest {
        val vm = getVm()
        advanceUntilIdle()

        vm.dispatch(BoardIntent.SetBoardSize(4))
        advanceUntilIdle()

        vm.dispatch(BoardIntent.ClickCell(Cell(row = 0, col = 1)))
        vm.dispatch(BoardIntent.ClickCell(Cell(row = 1, col = 3)))
        vm.dispatch(BoardIntent.ClickCell(Cell(row = 2, col = 0)))
        vm.dispatch(BoardIntent.ClickCell(Cell(row = 3, col = 2)))
        advanceUntilIdle()

        val latestRank: LatestRank? = vm.state.value.latestRank
        assertNotNull("latestRank must be set after winning", latestRank)
    }

    @Test
    fun `conflict is cleared in state after removing the conflicting queen`() = runTest {
        val vm = getVm()
        advanceUntilIdle()

        // Place two queens in the same column — conflict
        vm.dispatch(BoardIntent.ClickCell(Cell(0, 0)))
        vm.dispatch(BoardIntent.ClickCell(Cell(1, 0)))
        advanceUntilIdle()

        assertTrue(vm.state.value.conflicts.hasConflicts)

        // Remove the second queen
        vm.dispatch(BoardIntent.ClickCell(Cell(1, 0)))
        advanceUntilIdle()

        assertFalse(vm.state.value.conflicts.hasConflicts)
    }
}
