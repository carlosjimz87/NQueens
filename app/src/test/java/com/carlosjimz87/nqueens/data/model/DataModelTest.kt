package com.carlosjimz87.nqueens.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DataModelTest {

    @Test
    fun `ScoreEntry equality is based on all fields`() {
        val e1 = ScoreEntry(id = "abc", size = 8, timeMillis = 30_000L, moves = 20, epochMillis = 1000L)
        val e2 = ScoreEntry(id = "abc", size = 8, timeMillis = 30_000L, moves = 20, epochMillis = 1000L)
        assertEquals(e1, e2)
    }

    @Test
    fun `ScoreEntry not equal when id differs`() {
        val e1 = ScoreEntry(id = "abc", size = 8, timeMillis = 30_000L, moves = 20, epochMillis = 1000L)
        val e2 = ScoreEntry(id = "xyz", size = 8, timeMillis = 30_000L, moves = 20, epochMillis = 1000L)
        assertNotEquals(e1, e2)
    }

    @Test
    fun `StatsState default has empty entries list`() {
        val state = StatsState()
        assertTrue(state.entries.isEmpty())
    }

    @Test
    fun `LatestRank holds rankByTime and rankByMoves`() {
        val rank = LatestRank(rankByTime = 2, rankByMoves = 5)
        assertEquals(2, rank.rankByTime)
        assertEquals(5, rank.rankByMoves)
    }

    @Test
    fun `RecordResult holds entry and both ranks`() {
        val entry = ScoreEntry(id = "id1", size = 4, timeMillis = 10_000L, moves = 8, epochMillis = 0L)
        val result = RecordResult(entry = entry, rankByTime = 1, rankByMoves = 3)
        assertEquals(entry, result.entry)
        assertEquals(1, result.rankByTime)
        assertEquals(3, result.rankByMoves)
    }
}
