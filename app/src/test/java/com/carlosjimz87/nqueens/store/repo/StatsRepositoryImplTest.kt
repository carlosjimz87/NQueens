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
    fun `record appends when no existing (size+moves) and leaderboards reflect it`() = runTest {
        val initial = StatsState(
            entries = listOf(
                entry("a", size = 8, time = 50_000, moves = 30),
                entry("b", size = 8, time = 40_000, moves = 40),
                entry("c", size = 10, time = 10_000, moves = 10),
            )
        )
        val store = FakeStoreManager(initial)
        val repo = StatsRepositoryImpl(store)

        val result = repo.record(size = 8, timeMillis = 35_000, moves = 25, limit = 10)

        // It must be stored (append branch)
        val saved8 = store.current().entries.filter { it.size == 8 }
        assertEquals(3, saved8.size)
        assertTrue(saved8.any { it.id == result.entry.id })

        // And leaderboards must reflect the new entry
        val lb = repo.leaderboards(size = 8, limit = 10).first()
        assertTrue(lb.byTime.any { it.id == result.entry.id })
        assertTrue(lb.byMoves.any { it.id == result.entry.id })

        // ranks should be non-zero because it’s in top limit
        assertTrue(result.rankByTime > 0)
        assertTrue(result.rankByMoves > 0)
    }

    @Test
    fun `record replaces existing when same (size+moves) but better time`() = runTest {
        val initial = StatsState(
            entries = listOf(
                entry("old", size = 8, time = 50_000, moves = 20),
                entry("x", size = 8, time = 40_000, moves = 30),
            )
        )
        val store = FakeStoreManager(initial)
        val repo = StatsRepositoryImpl(store)

        val result = repo.record(size = 8, timeMillis = 10_000, moves = 20, limit = 10)

        // Replace branch: "old" must be removed, newEntry inserted
        val saved = store.current().entries.filter { it.size == 8 }
        assertEquals(2, saved.size)
        assertTrue(saved.none { it.id == "old" })
        assertTrue(saved.any { it.id == result.entry.id && it.moves == 20 && it.timeMillis.toInt() == 10_000 })

        // ranks should be 1 by time (it’s best)
        assertEquals(1, result.rankByTime)
    }

    @Test
    fun `record ignores when same (size+moves) but worse or equal time`() = runTest {
        val initial = StatsState(
            entries = listOf(
                entry("best", size = 8, time = 10_000, moves = 20),
                entry("x", size = 8, time = 40_000, moves = 30),
            )
        )
        val store = FakeStoreManager(initial)
        val repo = StatsRepositoryImpl(store)

        val before = store.current().entries

        val result = repo.record(size = 8, timeMillis = 10_000, moves = 20, limit = 10) // equal time => ignore

        val after = store.current().entries
        assertEquals(before, after) // store unchanged

        // because newEntry wasn’t inserted, it can’t rank
        assertEquals(0, result.rankByTime)
        assertEquals(0, result.rankByMoves)

        // leaderboards should remain unchanged too
        val lb = repo.leaderboards(size = 8, limit = 10).first()
        assertEquals(listOf("best", "x"), lb.byTime.map { it.id })
    }

    @Test
    fun `leaderboards returns empty for size with no entries`() = runTest {
        val store = FakeStoreManager(
            StatsState(entries = listOf(entry("a", size = 8, time = 10_000, moves = 10)))
        )
        val repo = StatsRepositoryImpl(store)

        val lb = repo.leaderboards(size = 99, limit = 10).first()

        assertTrue(lb.byTime.isEmpty())
        assertTrue(lb.byMoves.isEmpty())
    }
}