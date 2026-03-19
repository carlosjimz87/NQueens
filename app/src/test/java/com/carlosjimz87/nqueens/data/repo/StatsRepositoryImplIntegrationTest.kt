package com.carlosjimz87.nqueens.data.repo

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.stringPreferencesKey
import com.carlosjimz87.nqueens.data.model.StatsState
import com.carlosjimz87.nqueens.data.sources.StoreManagerImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

/**
 * Integration tests for [StatsRepositoryImpl] using a real [StoreManagerImpl]
 * backed by a temporary DataStore file.
 *
 * These tests verify the full pipeline: record -> JSON serialization -> DataStore persistence
 * -> deserialization -> leaderboards/getSortedScores, without any fakes.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StatsRepositoryImplIntegrationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
    }

    private val key = stringPreferencesKey("stats_state_json")

    private fun prefsFile(name: String): File {
        val dir = createTempDirectory("repo_integration_").toFile()
        return File(dir, "$name.preferences_pb")
    }

    private fun TestScope.createRepo(file: File): StatsRepositoryImpl {
        val ds = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { file }
        )
        val store = StoreManagerImpl(
            dataStore = ds,
            key = key,
            serializer = StatsState.serializer(),
            defaultValue = StatsState(),
            json = json
        )
        return StatsRepositoryImpl(store)
    }

    @Test
    fun `empty store yields empty leaderboards`() = runTest {
        val repo = createRepo(prefsFile("empty_lb"))

        val lb = repo.leaderboards(size = 8, limit = 10).first()

        assertTrue(lb.byTime.isEmpty())
        assertTrue(lb.byMoves.isEmpty())
    }

    @Test
    fun `empty store yields empty stats`() = runTest {
        val repo = createRepo(prefsFile("empty_stats"))

        val stats = repo.stats.first()

        assertTrue(stats.entries.isEmpty())
    }

    @Test
    fun `record persists through real DataStore and appears in leaderboards`() = runTest {
        val repo = createRepo(prefsFile("record_persist"))

        val result = repo.record(size = 8, timeMillis = 25_000, moves = 15, limit = 10)

        // Verify the result
        assertEquals(8, result.entry.size)
        assertEquals(25_000L, result.entry.timeMillis)
        assertEquals(15, result.entry.moves)
        assertTrue(result.rankByTime > 0)
        assertTrue(result.rankByMoves > 0)

        // Verify leaderboards reflect the entry
        val lb = repo.leaderboards(size = 8, limit = 10).first()
        assertEquals(1, lb.byTime.size)
        assertEquals(result.entry.id, lb.byTime.first().id)
    }

    @Test
    fun `multiple records for same size produce correct leaderboard ordering`() = runTest {
        val repo = createRepo(prefsFile("ordering"))

        // Record 3 entries with different times and moves
        repo.record(size = 8, timeMillis = 30_000, moves = 20, limit = 10)
        repo.record(size = 8, timeMillis = 10_000, moves = 40, limit = 10)
        repo.record(size = 8, timeMillis = 20_000, moves = 10, limit = 10)

        val lb = repo.leaderboards(size = 8, limit = 10).first()

        // byTime: 10_000 < 20_000 < 30_000
        assertEquals(
            listOf(10_000L, 20_000L, 30_000L),
            lb.byTime.map { it.timeMillis }
        )

        // byMoves: 10 < 20 < 40
        assertEquals(
            listOf(10, 20, 40),
            lb.byMoves.map { it.moves }
        )
    }

    @Test
    fun `record replaces existing entry when same size and moves but better time`() = runTest {
        val repo = createRepo(prefsFile("replace"))

        val first = repo.record(size = 8, timeMillis = 50_000, moves = 15, limit = 10)
        val second = repo.record(size = 8, timeMillis = 20_000, moves = 15, limit = 10)

        val stats = repo.stats.first()
        // The old entry should be replaced, not appended
        assertEquals(1, stats.entries.filter { it.size == 8 && it.moves == 15 }.size)
        // The remaining entry should be the one with better time
        assertEquals(second.entry.id, stats.entries.first { it.moves == 15 }.id)
    }

    @Test
    fun `record does not replace when same size and moves but worse time`() = runTest {
        val repo = createRepo(prefsFile("no_replace"))

        val first = repo.record(size = 8, timeMillis = 10_000, moves = 15, limit = 10)
        val second = repo.record(size = 8, timeMillis = 50_000, moves = 15, limit = 10)

        val stats = repo.stats.first()
        val matching = stats.entries.filter { it.moves == 15 }
        // No replacement should occur; original entry remains
        assertEquals(1, matching.size)
        // The entry kept is NOT the second one (worse time was ignored)
        assertEquals(0, second.rankByTime)
        assertEquals(0, second.rankByMoves)
    }

    @Test
    fun `records for different sizes are isolated in leaderboards`() = runTest {
        val repo = createRepo(prefsFile("size_isolation"))

        repo.record(size = 4, timeMillis = 5_000, moves = 8, limit = 10)
        repo.record(size = 8, timeMillis = 15_000, moves = 20, limit = 10)
        repo.record(size = 12, timeMillis = 60_000, moves = 50, limit = 10)

        val lb4 = repo.leaderboards(size = 4, limit = 10).first()
        val lb8 = repo.leaderboards(size = 8, limit = 10).first()
        val lb12 = repo.leaderboards(size = 12, limit = 10).first()

        assertEquals(1, lb4.byTime.size)
        assertEquals(1, lb8.byTime.size)
        assertEquals(1, lb12.byTime.size)

        assertEquals(4, lb4.byTime.first().size)
        assertEquals(8, lb8.byTime.first().size)
        assertEquals(12, lb12.byTime.first().size)
    }

    @Test
    fun `leaderboard limit is respected with real DataStore`() = runTest {
        val repo = createRepo(prefsFile("limit"))

        // Record 5 entries with distinct moves
        repeat(5) { i ->
            repo.record(size = 8, timeMillis = (i + 1) * 10_000L, moves = (i + 1) * 5, limit = 10)
        }

        val lb = repo.leaderboards(size = 8, limit = 3).first()

        assertEquals(3, lb.byTime.size)
        assertEquals(3, lb.byMoves.size)
    }

    @Test
    fun `full pipeline - record, query, record again, verify cumulative state`() = runTest {
        val repo = createRepo(prefsFile("full_pipeline"))

        // Step 1: Record first entry
        val r1 = repo.record(size = 8, timeMillis = 30_000, moves = 25, limit = 10)
        assertEquals(1, r1.rankByTime)

        // Step 2: Verify stats
        val stats1 = repo.stats.first()
        assertEquals(1, stats1.entries.size)

        // Step 3: Record second entry (better time, different moves)
        val r2 = repo.record(size = 8, timeMillis = 10_000, moves = 15, limit = 10)
        assertEquals(1, r2.rankByTime) // best time

        // Step 4: Verify cumulative state
        val stats2 = repo.stats.first()
        assertEquals(2, stats2.entries.size)

        // Step 5: Leaderboards reflect both
        val lb = repo.leaderboards(size = 8, limit = 10).first()
        assertEquals(2, lb.byTime.size)
        assertEquals(r2.entry.id, lb.byTime.first().id) // fastest first
        assertEquals(r2.entry.id, lb.byMoves.first().id) // fewest moves first
    }
}
