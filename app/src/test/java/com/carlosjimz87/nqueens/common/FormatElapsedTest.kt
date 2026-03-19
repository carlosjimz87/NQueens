package com.carlosjimz87.nqueens.common

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatElapsedTest {

    @Test
    fun `zero millis returns 00 colon 00`() {
        assertEquals("00:00", formatElapsed(0L))
    }

    @Test
    fun `sub-second millis truncates to 00 colon 00`() {
        assertEquals("00:00", formatElapsed(999L))
    }

    @Test
    fun `exactly 1000 millis returns 00 colon 01`() {
        assertEquals("00:01", formatElapsed(1_000L))
    }

    @Test
    fun `59 seconds returns 00 colon 59`() {
        assertEquals("00:59", formatElapsed(59_000L))
    }

    @Test
    fun `60 seconds returns 01 colon 00`() {
        assertEquals("01:00", formatElapsed(60_000L))
    }

    @Test
    fun `61 seconds returns 01 colon 01`() {
        assertEquals("01:01", formatElapsed(61_000L))
    }

    @Test
    fun `over one hour formats minutes beyond 59`() {
        assertEquals("61:01", formatElapsed(3_661_000L))
    }

    @Test
    fun `ten minutes returns 10 colon 00`() {
        assertEquals("10:00", formatElapsed(600_000L))
    }
}
