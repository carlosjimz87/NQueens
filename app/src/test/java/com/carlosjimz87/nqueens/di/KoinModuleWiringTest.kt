package com.carlosjimz87.nqueens.di

import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.context.GlobalContext.get
import com.carlosjimz87.nqueens.timer.GameTimer
import com.carlosjimz87.nqueens.board.BoardViewModel

/**
 * Integration test verifying that the Koin dependency graph resolves all
 * critical bindings without crashing.
 */
class KoinModuleWiringTest {

    @Before
    fun setUp() {
        startKoin { modules(appModule) }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `resolves GameTimer`() {
        val timer: GameTimer = get().get()
        assertNotNull(timer)
    }

    @Test
    fun `resolves BoardViewModel`() {
        val vm: BoardViewModel = get().get()
        assertNotNull(vm)
    }
}
