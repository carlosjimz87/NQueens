package com.carlosjimz87.nqueens.integration

import app.cash.turbine.test
import com.carlosjimz87.nqueens.data.model.StatsState
import com.carlosjimz87.nqueens.data.repo.StatsRepositoryImpl
import com.carlosjimz87.nqueens.data.sources.FakeStoreManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Integration tests verifying the reactive data flow between [StatsRepositoryImpl]
 * and [FakeStoreManager]. These tests confirm that the leaderboard [Flow] correctly
 * reflects state changes triggered by [StatsRepositoryImpl.record].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StatsFlowIntegrationTest {

    private fun freshRepo(): Pair<StatsRepositoryImpl, FakeStoreManager<StatsState>> {
        val store = FakeStoreManager(StatsState())
        val repo = StatsRepositoryImpl(store)
        return repo to store
    }

    @Test
    fun `leaderboard flow emits empty lists before any record is stored`() = runTest {
        val (repo, _) = freshRepo()
        val lb = repo.leaderboards(size = 8, limit = 10).test {
            val first = awaitItem()
            assertTrue(first.byTime.isEmpty())
            assertTrue(first.byMoves.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `leaderboard flow emits updated entry immediately after record is called`() = runTest {
        val (repo, _) = freshRepo()

        repo.leaderboards(size = 8, limit = 10).test {
            val initial = awaitItem()
            assertTrue(initial.byTime.isEmpty())

            repo.record(size = 8, timeMillis = 30_000L, moves = 15)

            val updated = awaitItem()
            assertFalse(updated.byTime.isEmpty())
            assertEquals(15, updated.byTime.first().moves)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `leaderboard byTime is ordered ascending by timeMillis after multiple records`() = runTest {
        val (repo, _) = freshRepo()

        repo.record(size = 8, timeMillis = 50_000L, moves = 10)
        repo.record(size = 8, timeMillis = 20_000L, moves = 15)
        repo.record(size = 8, timeMillis = 35_000L, moves = 12)

        val lb = repo.leaderboards(size = 8, limit = 10)
        lb.test {
            val item = awaitItem()
            val times = item.byTime.map { it.timeMillis }
            assertEquals(listOf(20_000L, 35_000L, 50_000L), times)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `leaderboard respects the limit parameter and only returns top N entries`() = runTest {
        val (repo, _) = freshRepo()

        for (i in 1..5) {
            repo.record(size = 8, timeMillis = i * 10_000L, moves = i)
        }

        val lb = repo.leaderboards(size = 8, limit = 3)
        lb.test {
            val item = awaitItem()
            assertEquals(3, item.byTime.size)
            assertEquals(3, item.byMoves.size)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
