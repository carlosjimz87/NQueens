package com.carlosjimz87.nqueens.presentation.timer

import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Tests for [CoroutineGameTimer].
 *
 * Because [CoroutineGameTimer] creates its own CoroutineScope with Dispatchers.Default
 * and measures wall-clock time via System.currentTimeMillis, these tests use real delays
 * and time-based tolerances instead of virtual time advancement.
 *
 * A short [TICK_MS] (50 ms) keeps total suite runtime well under 5 seconds.
 */
class CoroutineGameTimerTest {

    companion object {
        private const val TICK_MS = 50L
        private const val WAIT_TICKS = 150L
        private const val TOLERANCE_MS = 100L
    }

    private lateinit var timer: CoroutineGameTimer

    @Before
    fun setUp() {
        timer = CoroutineGameTimer(tickMillis = TICK_MS)
    }

    @After
    fun tearDown() {
        timer.reset()
    }

    @Test
    fun `initial elapsed is zero`() {
        assertEquals(0L, timer.elapsedMillis.value)
    }

    @Test
    fun `start begins ticking and elapsed increases`() = runBlocking {
        timer.start()
        Thread.sleep(WAIT_TICKS)

        val elapsed = timer.elapsedMillis.value
        assertTrue(
            "Expected elapsed > 0 after ${WAIT_TICKS}ms, but was $elapsed",
            elapsed > 0
        )
        assertTrue(
            "Expected elapsed <= ${WAIT_TICKS + TOLERANCE_MS}ms, but was $elapsed",
            elapsed <= WAIT_TICKS + TOLERANCE_MS
        )
    }

    @Test
    fun `stop pauses the timer and elapsed stays constant`() = runBlocking {
        timer.start()
        Thread.sleep(WAIT_TICKS)

        timer.stop()
        val snapshot = timer.elapsedMillis.value
        assertTrue("Timer should have ticked before stop, but elapsed was $snapshot", snapshot > 0)

        Thread.sleep(WAIT_TICKS)

        val afterWait = timer.elapsedMillis.value
        assertEquals(
            "Elapsed should not change after stop (snapshot=$snapshot, afterWait=$afterWait)",
            snapshot,
            afterWait
        )
    }

    @Test
    fun `reset clears elapsed to zero and stops the timer`() = runBlocking {
        timer.start()
        Thread.sleep(WAIT_TICKS)
        assertTrue("Timer should have ticked before reset", timer.elapsedMillis.value > 0)

        timer.reset()
        assertEquals("Elapsed should be 0 after reset", 0L, timer.elapsedMillis.value)

        Thread.sleep(WAIT_TICKS)
        assertEquals(
            "Elapsed should remain 0 after reset (timer should be stopped)",
            0L,
            timer.elapsedMillis.value
        )
    }

    @Test
    fun `start while already running is a no-op and does not restart`() = runBlocking {
        timer.start()
        Thread.sleep(WAIT_TICKS)
        val beforeSecondStart = timer.elapsedMillis.value
        assertTrue("Timer should have ticked", beforeSecondStart > 0)

        timer.start()

        Thread.sleep(WAIT_TICKS)
        val afterSecondStart = timer.elapsedMillis.value

        assertTrue(
            "Elapsed should continue growing after redundant start() " +
                "(before=$beforeSecondStart, after=$afterSecondStart)",
            afterSecondStart >= beforeSecondStart
        )
    }

    @Test
    fun `stop when not running does not crash`() {
        timer.stop()
        assertEquals("Elapsed should still be 0", 0L, timer.elapsedMillis.value)
    }

    @Test
    fun `stop can be called multiple times without error`() = runBlocking {
        timer.start()
        Thread.sleep(TICK_MS * 2)
        timer.stop()
        timer.stop()
        timer.stop()
        assertTrue("Elapsed should be > 0 after running and stopping", timer.elapsedMillis.value > 0)
    }

    @Test
    fun `start after stop resumes elapsed from previous value`() = runBlocking {
        timer.start()
        Thread.sleep(WAIT_TICKS)

        timer.stop()
        val snapshot = timer.elapsedMillis.value
        assertTrue("Timer should have ticked before stop", snapshot > 0)

        timer.start()
        Thread.sleep(WAIT_TICKS)

        timer.stop()
        val resumed = timer.elapsedMillis.value
        assertTrue(
            "After resume, elapsed ($resumed) should exceed snapshot ($snapshot)",
            resumed > snapshot
        )
        assertTrue(
            "After resume, elapsed ($resumed) should be at most " +
                "${2 * WAIT_TICKS + TOLERANCE_MS}ms",
            resumed <= 2 * WAIT_TICKS + TOLERANCE_MS
        )
    }

    @Test
    fun `reset while running then start produces fresh timing`() = runBlocking {
        timer.start()
        Thread.sleep(WAIT_TICKS)
        assertTrue("Timer should have ticked", timer.elapsedMillis.value > 0)

        timer.reset()
        assertEquals(0L, timer.elapsedMillis.value)

        timer.start()
        Thread.sleep(WAIT_TICKS)

        val elapsed = timer.elapsedMillis.value
        assertTrue("Fresh start after reset should tick", elapsed > 0)
        assertTrue(
            "Fresh start elapsed ($elapsed) should be within one WAIT window + tolerance",
            elapsed <= WAIT_TICKS + TOLERANCE_MS
        )
    }

    @Test
    fun `elapsed flow value is non-negative at all times`() = runBlocking {
        timer.start()
        repeat(5) {
            Thread.sleep(TICK_MS)
            assertTrue(
                "Elapsed should never be negative, was ${timer.elapsedMillis.value}",
                timer.elapsedMillis.value >= 0
            )
        }
        timer.stop()
    }
}
