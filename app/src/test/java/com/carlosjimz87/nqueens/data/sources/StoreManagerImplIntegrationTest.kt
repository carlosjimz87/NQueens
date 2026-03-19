package com.carlosjimz87.nqueens.data.sources

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.carlosjimz87.nqueens.data.model.ScoreEntry
import com.carlosjimz87.nqueens.data.model.StatsState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

/**
 * Integration tests for [StoreManagerImpl] that exercise real DataStore read/write
 * round-trips with actual JSON serialization through the full pipeline.
 *
 * These tests differ from the existing [StoreManagerImplTest] by focusing on:
 * - Multi-step write-then-read round-trips
 * - Sequential updates building on prior state
 * - Concurrent update safety
 * - Flow reactivity across multiple emissions
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StoreManagerImplIntegrationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
    }

    private val key = stringPreferencesKey("stats_state_json")

    private fun entry(
        id: String,
        size: Int = 8,
        time: Long = 10_000L,
        moves: Int = 10,
        epoch: Long = 0L
    ) = ScoreEntry(id = id, size = size, timeMillis = time, moves = moves, epochMillis = epoch)

    private fun prefsFile(name: String): File {
        val dir = createTempDirectory("ds_integration_").toFile()
        return File(dir, "$name.preferences_pb")
    }

    private fun TestScope.createManager(file: File): StoreManagerImpl<StatsState> {
        val ds = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { file }
        )
        return StoreManagerImpl(
            dataStore = ds,
            key = key,
            serializer = StatsState.serializer(),
            defaultValue = StatsState(),
            json = json
        )
    }

    @Test
    fun `initial empty state round-trips correctly`() = runTest {
        val manager = createManager(prefsFile("empty_roundtrip"))

        val initial = manager.data.first()

        assertEquals(StatsState(), initial)
        assertTrue(initial.entries.isEmpty())
    }

    @Test
    fun `write then read preserves all ScoreEntry fields`() = runTest {
        val manager = createManager(prefsFile("field_roundtrip"))

        val original = entry(
            id = "uuid-123",
            size = 12,
            time = 45_678L,
            moves = 33,
            epoch = 1_700_000_000_000L
        )

        manager.update { StatsState(entries = listOf(original)) }

        val read = manager.data.first().entries.single()
        assertEquals("uuid-123", read.id)
        assertEquals(12, read.size)
        assertEquals(45_678L, read.timeMillis)
        assertEquals(33, read.moves)
        assertEquals(1_700_000_000_000L, read.epochMillis)
    }

    @Test
    fun `sequential updates accumulate entries correctly`() = runTest {
        val manager = createManager(prefsFile("sequential"))

        manager.update { it.copy(entries = it.entries + entry("first")) }
        manager.update { it.copy(entries = it.entries + entry("second")) }
        manager.update { it.copy(entries = it.entries + entry("third")) }

        val state = manager.data.first()
        assertEquals(3, state.entries.size)
        assertEquals(
            listOf("first", "second", "third"),
            state.entries.map { it.id }
        )
    }

    @Test
    fun `update reads current state and transforms it atomically`() = runTest {
        val manager = createManager(prefsFile("atomic_transform"))

        // Seed with 2 entries
        manager.update { StatsState(entries = listOf(entry("a"), entry("b"))) }

        // Remove entry "a" and add entry "c" in one update
        manager.update { current ->
            val filtered = current.entries.filter { it.id != "a" }
            current.copy(entries = filtered + entry("c"))
        }

        val result = manager.data.first()
        assertEquals(listOf("b", "c"), result.entries.map { it.id })
    }

    @Test
    fun `concurrent updates do not lose data`() = runTest {
        val manager = createManager(prefsFile("concurrent"))

        // Launch 10 concurrent updates, each appending a unique entry
        val jobs = (1..10).map { i ->
            async {
                manager.update { current ->
                    current.copy(entries = current.entries + entry("entry-$i"))
                }
            }
        }
        jobs.awaitAll()

        val state = manager.data.first()
        assertEquals(10, state.entries.size)

        val ids = state.entries.map { it.id }.toSet()
        assertEquals((1..10).map { "entry-$it" }.toSet(), ids)
    }

    @Test
    fun `flow emits updates reactively`() = runTest {
        val manager = createManager(prefsFile("reactive_flow"))

        // Collect exactly 3 emissions: initial (0 entries), after adding "a" (1 entry),
        // after adding "b" (2 entries). We use take(3) to avoid hanging indefinitely.
        val collectJob = async {
            manager.data.take(3).toList().map { it.entries.size }
        }

        // Trigger two updates; combined with the initial emission, that yields 3 values.
        manager.update { it.copy(entries = it.entries + entry("a")) }
        manager.update { it.copy(entries = it.entries + entry("b")) }

        val sizes = collectJob.await()
        assertEquals(listOf(0, 1, 2), sizes)
    }

    @Test
    fun `large payload serializes and deserializes correctly`() = runTest {
        val manager = createManager(prefsFile("large_payload"))

        val entries = (1..100).map { i ->
            entry(
                id = "id-$i",
                size = i % 20 + 4,
                time = i * 1_000L,
                moves = i * 2,
                epoch = 1_700_000_000_000L + i
            )
        }

        manager.update { StatsState(entries = entries) }

        val read = manager.data.first()
        assertEquals(100, read.entries.size)
        assertEquals("id-1", read.entries.first().id)
        assertEquals("id-100", read.entries.last().id)
    }

    @Test
    fun `update after corrupted write recovers with default and persists new value`() = runTest {
        val file = prefsFile("corrupt_recovery")
        val ds = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { file }
        )

        // Corrupt the store directly
        ds.updateData { prefs ->
            prefs.toMutablePreferences().apply {
                this[key] = "{{{{not valid json"
            }
        }

        val manager = StoreManagerImpl(
            dataStore = ds,
            key = key,
            serializer = StatsState.serializer(),
            defaultValue = StatsState(),
            json = json
        )

        // Update should fall back to default and then apply the transform
        manager.update { current ->
            current.copy(entries = current.entries + entry("recovered"))
        }

        val result = manager.data.first()
        assertEquals(1, result.entries.size)
        assertEquals("recovered", result.entries.first().id)
    }
}
