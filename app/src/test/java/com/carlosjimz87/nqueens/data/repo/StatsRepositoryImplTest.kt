package com.carlosjimz87.nqueens.data.repo

import com.carlosjimz87.nqueens.data.sources.FakeStoreManager
import com.carlosjimz87.nqueens.data.model.ScoreEntry
import com.carlosjimz87.nqueens.data.model.StatsState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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

    // ---- Existing tests (1-4) ----

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

        // ranks should be non-zero because it's in top limit
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

        // ranks should be 1 by time (it's best)
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

        // because newEntry wasn't inserted, it can't rank
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

    // ---- New tests (5-12) ----

    @Test
    fun `leaderboards respects limit parameter`() = runTest {
        val initial = StatsState(
            entries = listOf(
                entry("a", size = 8, time = 10_000, moves = 10),
                entry("b", size = 8, time = 20_000, moves = 20),
                entry("c", size = 8, time = 30_000, moves = 30),
                entry("d", size = 8, time = 40_000, moves = 40),
                entry("e", size = 8, time = 50_000, moves = 50),
            )
        )
        val store = FakeStoreManager(initial)
        val repo = StatsRepositoryImpl(store)

        val lb = repo.leaderboards(size = 8, limit = 3).first()

        assertEquals(3, lb.byTime.size)
        assertEquals(3, lb.byMoves.size)
    }

    @Test
    fun `leaderboards sorts by time ascending then by moves`() = runTest {
        val initial = StatsState(
            entries = listOf(
                entry("slow", size = 8, time = 50_000, moves = 5),
                entry("fast-many", size = 8, time = 10_000, moves = 30),
                entry("fast-few", size = 8, time = 10_000, moves = 10),
                entry("mid", size = 8, time = 30_000, moves = 20),
            )
        )
        val store = FakeStoreManager(initial)
        val repo = StatsRepositoryImpl(store)

        val lb = repo.leaderboards(size = 8, limit = 10).first()

        // Primary sort: timeMillis ascending; tiebreaker: moves ascending
        assertEquals(
            listOf("fast-few", "fast-many", "mid", "slow"),
            lb.byTime.map { it.id }
        )
    }

    @Test
    fun `leaderboards sorts by moves ascending then by time`() = runTest {
        val initial = StatsState(
            entries = listOf(
                entry("many-fast", size = 8, time = 10_000, moves = 50),
                entry("few-slow", size = 8, time = 40_000, moves = 5),
                entry("few-fast", size = 8, time = 10_000, moves = 5),
                entry("mid", size = 8, time = 20_000, moves = 20),
            )
        )
        val store = FakeStoreManager(initial)
        val repo = StatsRepositoryImpl(store)

        val lb = repo.leaderboards(size = 8, limit = 10).first()

        // Primary sort: moves ascending; tiebreaker: timeMillis ascending
        assertEquals(
            listOf("few-fast", "few-slow", "mid", "many-fast"),
            lb.byMoves.map { it.id }
        )
    }

    @Test
    fun `record for different sizes are independent`() = runTest {
        val store = FakeStoreManager(StatsState())
        val repo = StatsRepositoryImpl(store)

        repo.record(size = 4, timeMillis = 5_000, moves = 10, limit = 10)
        repo.record(size = 8, timeMillis = 15_000, moves = 20, limit = 10)

        val lb4 = repo.leaderboards(size = 4, limit = 10).first()
        val lb8 = repo.leaderboards(size = 8, limit = 10).first()

        // Size 4 leaderboard has exactly 1 entry with moves=10
        assertEquals(1, lb4.byTime.size)
        assertEquals(10, lb4.byTime[0].moves)

        // Size 8 leaderboard has exactly 1 entry with moves=20
        assertEquals(1, lb8.byTime.size)
        assertEquals(20, lb8.byTime[0].moves)

        // No cross-contamination: size 4 board has no entry with moves=20
        assertTrue(lb4.byTime.none { it.moves == 20 })
        assertTrue(lb8.byTime.none { it.moves == 10 })
    }

    @Test
    fun `stats flow emits updated state after record`() = runTest {
        val store = FakeStoreManager(StatsState())
        val repo = StatsRepositoryImpl(store)

        // Collect initial state
        val stateBefore = repo.stats.first()
        assertTrue(stateBefore.entries.isEmpty())

        // Record an entry
        val result = repo.record(size = 8, timeMillis = 10_000, moves = 15, limit = 10)

        // Flow should now reflect the new entry
        val stateAfter = repo.stats.first()
        assertEquals(1, stateAfter.entries.size)
        assertEquals(result.entry.id, stateAfter.entries[0].id)
    }

    @Test
    fun `record with limit truncates leaderboard rankings`() = runTest {
        val store = FakeStoreManager(StatsState())
        val repo = StatsRepositoryImpl(store)

        // Record 5 entries with unique moves so all are appended
        repeat(5) { i ->
            repo.record(size = 8, timeMillis = (i + 1) * 10_000L, moves = (i + 1) * 10, limit = 3)
        }

        // Total entries should be 5 in the store
        assertEquals(5, store.current().entries.filter { it.size == 8 }.size)

        // Leaderboard with limit=3 should only show 3
        val lb = repo.leaderboards(size = 8, limit = 3).first()
        assertEquals(3, lb.byTime.size)
        assertEquals(3, lb.byMoves.size)

        // The worst entry (time=50_000) should not appear in the limited leaderboard
        assertTrue(lb.byTime.none { it.timeMillis == 50_000L })
    }

    @Test
    fun `record multiple times same size different moves appends all`() = runTest {
        val store = FakeStoreManager(StatsState())
        val repo = StatsRepositoryImpl(store)

        val r1 = repo.record(size = 8, timeMillis = 10_000, moves = 10, limit = 10)
        val r2 = repo.record(size = 8, timeMillis = 20_000, moves = 20, limit = 10)
        val r3 = repo.record(size = 8, timeMillis = 30_000, moves = 30, limit = 10)

        val entries = store.current().entries
        assertEquals(3, entries.size)

        // All three should be present with distinct ids
        val ids = entries.map { it.id }.toSet()
        assertEquals(setOf(r1.entry.id, r2.entry.id, r3.entry.id), ids)

        // Each unique (size, moves) pair is represented
        val movesSet = entries.map { it.moves }.toSet()
        assertEquals(setOf(10, 20, 30), movesSet)
    }

    @Test
    fun `leaderboards is reactive to store changes`() = runTest {
        val store = FakeStoreManager(StatsState())
        val repo = StatsRepositoryImpl(store)

        val emissions = mutableListOf<Int>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            repo.leaderboards(size = 8, limit = 10).collect { lb ->
                emissions.add(lb.byTime.size)
            }
        }

        // Initial emission: 0 entries
        assertEquals(listOf(0), emissions)

        // Record first entry -> store updates -> flow re-emits
        repo.record(size = 8, timeMillis = 10_000, moves = 10, limit = 10)
        assertEquals(listOf(0, 1), emissions)

        // Record second entry -> another emission
        repo.record(size = 8, timeMillis = 20_000, moves = 20, limit = 10)
        assertEquals(listOf(0, 1, 2), emissions)

        job.cancel()
    }
}
