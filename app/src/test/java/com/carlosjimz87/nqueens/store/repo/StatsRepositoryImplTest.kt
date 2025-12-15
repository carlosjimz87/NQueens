package com.carlosjimz87.nqueens.store.repo

import com.carlosjimz87.nqueens.store.manager.FakeStoreManager
import com.carlosjimz87.nqueens.store.model.ScoreEntry
import com.carlosjimz87.nqueens.store.model.StatsState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test


@OptIn(ExperimentalCoroutinesApi::class)
class StatsRepositoryImplTest {

    private fun entry(
        id: String,
        size: Int,
        time: Long,
        moves: Int,
        epoch: Long = 0L
    ) = ScoreEntry(
        id = id,
        size = size,
        timeMillis = time,
        moves = moves,
        epochMillis = epoch
    )

    @Test
    fun `leaderboards filters by size and sorts correctly with limit`() = runTest {
        val initial = StatsState(
            entries = listOf(
                entry("a", size = 8, time = 50_000, moves = 30),
                entry("b", size = 8, time = 40_000, moves = 40),
                entry("c", size = 8, time = 40_000, moves = 20), // tie on time, fewer moves wins
                entry("d", size = 10, time = 10_000, moves = 10) // different size, must be ignored
            )
        )
        val store = FakeStoreManager(initial)
        val repo = StatsRepositoryImpl(store)

        val lb = repo.leaderboards(size = 8, limit = 2).first()

        assertEquals(listOf("c", "b"), lb.byTime.map { it.id })

        assertEquals(listOf("c", "a"), lb.byMoves.map { it.id })
    }

    @Test
    fun `record adds entry to store`() = runTest {
        val store = FakeStoreManager(StatsState(entries = emptyList()))
        val repo = StatsRepositoryImpl(store)

        val result = repo.record(size = 8, timeMillis = 12_000, moves = 18, limit = 10)

        val saved = store.current().entries
        assertEquals(1, saved.size)
        assertEquals(result.entry.id, saved.first().id)
        assertEquals(8, saved.first().size)
        assertEquals(12_000, saved.first().timeMillis)
        assertEquals(18, saved.first().moves)
        assertTrue(saved.first().epochMillis > 0L)
    }

    @Test
    fun `record returns rank 1 when best time and moves in top limit`() = runTest {
        val initial = StatsState(
            entries = listOf(
                entry("x1", size = 8, time = 50_000, moves = 40),
                entry("x2", size = 8, time = 60_000, moves = 10)
            )
        )
        val store = FakeStoreManager(initial)
        val repo = StatsRepositoryImpl(store)

        val result = repo.record(size = 8, timeMillis = 10_000, moves = 5, limit = 10)

        assertEquals(1, result.rankByTime)
        assertEquals(1, result.rankByMoves)
    }

    @Test
    fun `record ranks are 0 if not in top limit`() = runTest {
        val initial = StatsState(
            entries = listOf(
                entry("best1", size = 8, time = 10_000, moves = 10),
                entry("best2", size = 8, time = 11_000, moves = 11)
            )
        )
        val store = FakeStoreManager(initial)
        val repo = StatsRepositoryImpl(store)

        val result = repo.record(size = 8, timeMillis = 99_000, moves = 99, limit = 2)

        assertEquals(0, result.rankByTime)
        assertEquals(0, result.rankByMoves)
    }

    @Test
    fun `record does not affect rankings for other sizes`() = runTest {
        val initial = StatsState(
            entries = listOf(
                entry("s8_1", size = 8, time = 5_000, moves = 5),
                entry("s10_1", size = 10, time = 6_000, moves = 6),
                entry("s10_2", size = 10, time = 7_000, moves = 7),
            )
        )
        val store = FakeStoreManager(initial)
        val repo = StatsRepositoryImpl(store)

        val result = repo.record(size = 8, timeMillis = 10_000, moves = 10, limit = 10)

        assertEquals(2, result.rankByTime)
        assertEquals(2, result.rankByMoves)

        val lb10 = repo.leaderboards(size = 10, limit = 10).first()
        assertEquals(2, lb10.byTime.size)
    }
}