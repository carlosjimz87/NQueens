package com.carlosjimz87.nqueens.common

import org.junit.Assert.assertEquals
import org.junit.Test

class UtilsTest {

    @Test
    fun `formatElapsed returns 00_00 for zero millis`() {
        assertEquals("00:00", formatElapsed(0L))
    }

    @Test
    fun `formatElapsed returns 00_01 for 1000 millis`() {
        assertEquals("00:01", formatElapsed(1_000L))
    }

    @Test
    fun `formatElapsed returns 01_00 for 60000 millis`() {
        assertEquals("01:00", formatElapsed(60_000L))
    }

    @Test
    fun `formatElapsed returns 01_30 for 90000 millis`() {
        assertEquals("01:30", formatElapsed(90_000L))
    }

    @Test
    fun `formatElapsed returns 10_05 for 605000 millis`() {
        assertEquals("10:05", formatElapsed(605_000L))
    }
}
