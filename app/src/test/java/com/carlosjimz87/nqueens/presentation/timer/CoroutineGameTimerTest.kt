package com.carlosjimz87.nqueens.presentation.timer

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CoroutineGameTimerTest {

    @Test
    fun `initial elapsed is zero`() {
        val timer = CoroutineGameTimer()
        assertEquals(0L, timer.elapsedMillis.value)
    }

    @Test
    fun `reset without starting keeps elapsed at zero`() {
        val timer = CoroutineGameTimer()
        timer.reset()
        assertEquals(0L, timer.elapsedMillis.value)
    }

    @Test
    fun `stop without starting does not throw and elapsed remains zero`() {
        val timer = CoroutineGameTimer()
        timer.stop()
        assertEquals(0L, timer.elapsedMillis.value)
    }

    @Test
    fun `elapsed is non-zero after start and delay`() = runBlocking {
        val timer = CoroutineGameTimer(tickMillis = 50L)
        timer.start()
        delay(200L)
        timer.stop()
        assertTrue("Elapsed should be greater than zero after ticking", timer.elapsedMillis.value > 0L)
    }

    @Test
    fun `elapsed does not increment after stop`() = runBlocking {
        val timer = CoroutineGameTimer(tickMillis = 50L)
        timer.start()
        delay(150L)
        timer.stop()
        delay(50L) // let any in-flight delay finish
        val snapshot = timer.elapsedMillis.value
        delay(200L) // more real time passes, but timer is stopped
        assertEquals("Elapsed must not change after stop", snapshot, timer.elapsedMillis.value)
    }
}
