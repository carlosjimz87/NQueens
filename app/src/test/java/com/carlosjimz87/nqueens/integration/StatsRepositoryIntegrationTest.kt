package com.carlosjimz87.nqueens.integration

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.stringPreferencesKey
import com.carlosjimz87.nqueens.data.model.StatsState
import com.carlosjimz87.nqueens.data.repo.StatsRepositoryImpl
import com.carlosjimz87.nqueens.data.sources.StoreManagerImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

/**
 * Integration tests for [StatsRepositoryImpl] backed by a real [StoreManagerImpl]
 * with a real DataStore (temporary files).
 *
 * These tests verify the full record -> leaderboards -> getSortedScores pipeline
 * without any fakes, exercising the real serialization and persistence layer.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StatsRepositoryIntegrationTest {

    private val testKey = stringPreferencesKey("stats_test")

    private fun createRepo(
        scope: kotlinx.coroutines.CoroutineScope
    ): StatsRepositoryImpl {
        val dir = createTempDirectory("repo_test").toFile()
        val file = File(dir, "repo_prefs.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(scope = scope) { file }
        val store = StoreManagerImpl(
            dataStore = dataStore,
            key = testKey,
            serializer = StatsState.serializer(),
            defaultValue = StatsState()
        )
        return StatsRepositoryImpl(store)
    }

    // -- Empty store --

    @Test
    fun `empty store returns empty leaderboards`() = runTest {
        val repo = createRepo(backgroundScope)

        val leaderboards = repo.leaderboards(size = 4).first()

        assertTrue(leaderboards.byTime.isEmpty())
        assertTrue(leaderboards.byMoves.isEmpty())
    }

    @Test
    fun `empty store stats has no entries`() = runTest {
        val repo = createRepo(backgroundScope)

        val stats = repo.stats.first()

        assertTrue(stats.entries.isEmpty())
    }

    // -- Record persists --

    @Test
    fun `recorded score persists and appears in leaderboards`() = runTest {
        val repo = createRepo(backgroundScope)

        val result = repo.record(size = 4, timeMillis = 5000L, moves = 8)

        assertEquals(4, result.entry.size)
        assertEquals(5000L, result.entry.timeMillis)
        assertEquals(8, result.entry.moves)
        assertTrue(result.rankByTime > 0)
        assertTrue(result.rankByMoves > 0)

        val leaderboards = repo.leaderboards(size = 4).first()

        assertEquals(1, leaderboards.byTime.size)
        assertEquals(result.entry.id, leaderboards.byTime[0].id)
    }

    // -- Multiple records ordering --

    @Test
    fun `multiple records ordered correctly by time`() = runTest {
        val repo = createRepo(backgroundScope)

        repo.record(size = 4, timeMillis = 3000L, moves = 10)
        repo.record(size = 4, timeMillis = 1000L, moves = 12)
        repo.record(size = 4, timeMillis = 5000L, moves = 8)

        val leaderboards = repo.leaderboards(size = 4).first()

        assertEquals(3, leaderboards.byTime.size)
        assertEquals(1000L, leaderboards.byTime[0].timeMillis)
        assertEquals(3000L, leaderboards.byTime[1].timeMillis)
        assertEquals(5000L, leaderboards.byTime[2].timeMillis)
    }

    @Test
    fun `multiple records ordered correctly by moves`() = runTest {
        val repo = createRepo(backgroundScope)

        repo.record(size = 4, timeMillis = 3000L, moves = 10)
        repo.record(size = 4, timeMillis = 1000L, moves = 12)
        repo.record(size = 4, timeMillis = 5000L, moves = 8)

        val leaderboards = repo.leaderboards(size = 4).first()

        assertEquals(3, leaderboards.byMoves.size)
        assertEquals(8, leaderboards.byMoves[0].moves)
        assertEquals(10, leaderboards.byMoves[1].moves)
        assertEquals(12, leaderboards.byMoves[2].moves)
    }

    // -- Replace on better time --

    @Test
    fun `replaces entry when same size and moves but better time`() = runTest {
        val repo = createRepo(backgroundScope)

        val first = repo.record(size = 4, timeMillis = 5000L, moves = 8)
        val second = repo.record(size = 4, timeMillis = 3000L, moves = 8)

        // The second record should replace the first (same size + moves, better time)
        assertNotEquals(first.entry.id, second.entry.id)

        val leaderboards = repo.leaderboards(size = 4).first()

        assertEquals(1, leaderboards.byTime.size)
        assertEquals(3000L, leaderboards.byTime[0].timeMillis)
        assertEquals(second.entry.id, leaderboards.byTime[0].id)
    }

    // -- No replace on worse time --

    @Test
    fun `does not replace entry when same size and moves but worse time`() = runTest {
        val repo = createRepo(backgroundScope)

        val first = repo.record(size = 4, timeMillis = 3000L, moves = 8)
        repo.record(size = 4, timeMillis = 5000L, moves = 8)

        val leaderboards = repo.leaderboards(size = 4).first()

        assertEquals(1, leaderboards.byTime.size)
        assertEquals(3000L, leaderboards.byTime[0].timeMillis)
        assertEquals(first.entry.id, leaderboards.byTime[0].id)
    }

    // -- Different sizes isolated --

    @Test
    fun `different board sizes are isolated in leaderboards`() = runTest {
        val repo = createRepo(backgroundScope)

        repo.record(size = 4, timeMillis = 1000L, moves = 4)
        repo.record(size = 5, timeMillis = 2000L, moves = 6)
        repo.record(size = 6, timeMillis = 3000L, moves = 8)

        val lb4 = repo.leaderboards(size = 4).first()
        val lb5 = repo.leaderboards(size = 5).first()
        val lb6 = repo.leaderboards(size = 6).first()

        assertEquals(1, lb4.byTime.size)
        assertEquals(4, lb4.byTime[0].size)

        assertEquals(1, lb5.byTime.size)
        assertEquals(5, lb5.byTime[0].size)

        assertEquals(1, lb6.byTime.size)
        assertEquals(6, lb6.byTime[0].size)
    }

    // -- Limit respected --

    @Test
    fun `leaderboard limit is respected`() = runTest {
        val repo = createRepo(backgroundScope)

        // Record 15 entries with different move counts (so they are all unique)
        for (i in 1..15) {
            repo.record(size = 4, timeMillis = i * 1000L, moves = i)
        }

        val limitedLeaderboards = repo.leaderboards(size = 4, limit = 5).first()

        assertEquals(5, limitedLeaderboards.byTime.size)
        assertEquals(5, limitedLeaderboards.byMoves.size)

        // byTime should have the 5 fastest times
        assertEquals(1000L, limitedLeaderboards.byTime[0].timeMillis)
        assertEquals(5000L, limitedLeaderboards.byTime[4].timeMillis)
    }

    // -- Rank correctness --

    @Test
    fun `record returns correct rank positions`() = runTest {
        val repo = createRepo(backgroundScope)

        val r1 = repo.record(size = 4, timeMillis = 5000L, moves = 10)
        assertEquals(1, r1.rankByTime)
        assertEquals(1, r1.rankByMoves)

        val r2 = repo.record(size = 4, timeMillis = 1000L, moves = 12)
        assertEquals(1, r2.rankByTime)  // fastest time
        assertEquals(2, r2.rankByMoves) // more moves

        val r3 = repo.record(size = 4, timeMillis = 3000L, moves = 8)
        assertEquals(2, r3.rankByTime)  // middle time
        assertEquals(1, r3.rankByMoves) // fewest moves
    }
}
