package com.carlosjimz87.nqueens.integration

import app.cash.turbine.test
import com.carlosjimz87.nqueens.data.model.StatsState
import com.carlosjimz87.nqueens.data.repo.StatsRepositoryImpl
import com.carlosjimz87.nqueens.data.sources.FakeStoreManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Integration tests for the reactive data flow through [StatsRepositoryImpl].
 *
 * Verifies that the repository's [leaderboards] Flow emits correct updates
 * after [record] calls, using Turbine for deterministic flow assertions.
 * Uses [FakeStoreManager] (which is an in-memory implementation with Mutex-based
 * thread safety) to keep these tests fast and JVM-only while still exercising
 * the real repository logic.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StatsFlowIntegrationTest {

    private fun createRepo(): StatsRepositoryImpl {
        val store = FakeStoreManager(StatsState())
        return StatsRepositoryImpl(store)
    }

    // -- Initial emission --

    @Test
    fun `leaderboards emits empty lists initially`() = runTest {
        val repo = createRepo()

        repo.leaderboards(size = 4).test {
            val initial = awaitItem()
            assertTrue(initial.byTime.isEmpty())
            assertTrue(initial.byMoves.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // -- Updated emission after record --

    @Test
    fun `leaderboards emits updated data after record`() = runTest {
        val repo = createRepo()

        repo.leaderboards(size = 4).test {
            // Initial empty emission
            val initial = awaitItem()
            assertTrue(initial.byTime.isEmpty())

            // Record a score
            repo.record(size = 4, timeMillis = 3000L, moves = 8)

            // Should receive updated leaderboard
            val updated = awaitItem()
            assertEquals(1, updated.byTime.size)
            assertEquals(3000L, updated.byTime[0].timeMillis)
            assertEquals(8, updated.byTime[0].moves)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // -- Ordering after multiple records --

    @Test
    fun `leaderboards maintains correct ordering after multiple records`() = runTest {
        val repo = createRepo()

        repo.leaderboards(size = 4).test {
            awaitItem() // initial empty

            repo.record(size = 4, timeMillis = 5000L, moves = 10)
            val after1 = awaitItem()
            assertEquals(1, after1.byTime.size)

            repo.record(size = 4, timeMillis = 2000L, moves = 12)
            val after2 = awaitItem()
            assertEquals(2, after2.byTime.size)
            // Fastest time should be first
            assertEquals(2000L, after2.byTime[0].timeMillis)
            assertEquals(5000L, after2.byTime[1].timeMillis)
            // Fewest moves should be first in byMoves
            assertEquals(10, after2.byMoves[0].moves)
            assertEquals(12, after2.byMoves[1].moves)

            repo.record(size = 4, timeMillis = 1000L, moves = 14)
            val after3 = awaitItem()
            assertEquals(3, after3.byTime.size)
            assertEquals(1000L, after3.byTime[0].timeMillis)
            assertEquals(2000L, after3.byTime[1].timeMillis)
            assertEquals(5000L, after3.byTime[2].timeMillis)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // -- Limit enforcement --

    @Test
    fun `leaderboards respects limit parameter`() = runTest {
        val repo = createRepo()

        // Record 8 entries with different move counts
        for (i in 1..8) {
            repo.record(size = 4, timeMillis = i * 1000L, moves = i)
        }

        repo.leaderboards(size = 4, limit = 3).test {
            val result = awaitItem()
            assertEquals(3, result.byTime.size)
            assertEquals(3, result.byMoves.size)

            // byTime should have the 3 fastest
            assertEquals(1000L, result.byTime[0].timeMillis)
            assertEquals(2000L, result.byTime[1].timeMillis)
            assertEquals(3000L, result.byTime[2].timeMillis)

            // byMoves should have the 3 fewest moves
            assertEquals(1, result.byMoves[0].moves)
            assertEquals(2, result.byMoves[1].moves)
            assertEquals(3, result.byMoves[2].moves)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // -- Different size isolation in flow --

    @Test
    fun `leaderboards flow does not emit for different board sizes`() = runTest {
        val repo = createRepo()

        repo.leaderboards(size = 4).test {
            awaitItem() // initial empty

            // Record for size 5 -- should still emit because underlying store changed,
            // but the filtered leaderboard for size 4 should remain empty
            repo.record(size = 5, timeMillis = 1000L, moves = 5)
            val afterSize5 = awaitItem()
            assertTrue(
                "Leaderboard for size 4 should be empty after recording for size 5",
                afterSize5.byTime.isEmpty()
            )

            // Now record for size 4
            repo.record(size = 4, timeMillis = 2000L, moves = 8)
            val afterSize4 = awaitItem()
            assertEquals(1, afterSize4.byTime.size)
            assertEquals(4, afterSize4.byTime[0].size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // -- Stats flow --

    @Test
    fun `stats flow emits all entries across all sizes`() = runTest {
        val repo = createRepo()

        repo.stats.test {
            val initial = awaitItem()
            assertTrue(initial.entries.isEmpty())

            repo.record(size = 4, timeMillis = 1000L, moves = 4)
            val after1 = awaitItem()
            assertEquals(1, after1.entries.size)

            repo.record(size = 8, timeMillis = 2000L, moves = 10)
            val after2 = awaitItem()
            assertEquals(2, after2.entries.size)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
