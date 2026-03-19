package com.carlosjimz87.nqueens.integration

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.carlosjimz87.nqueens.data.model.ScoreEntry
import com.carlosjimz87.nqueens.data.model.StatsState
import com.carlosjimz87.nqueens.data.sources.StoreManagerImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

/**
 * Integration tests for [StoreManagerImpl] using a real DataStore backed by temporary files.
 *
 * These tests verify the actual JSON serialization round-trip, reactive Flow emissions,
 * concurrent update safety, and corrupted data recovery -- all behaviors that cannot be
 * covered by the in-memory [FakeStoreManager].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StoreManagerImplIntegrationTest {

    private val testKey = stringPreferencesKey("test_stats")

    private fun createStoreManager(
        scope: kotlinx.coroutines.CoroutineScope
    ): StoreManagerImpl<StatsState> {
        val dir = createTempDirectory("datastore_test").toFile()
        val file = File(dir, "test_prefs.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(scope = scope) { file }
        return StoreManagerImpl(
            dataStore = dataStore,
            key = testKey,
            serializer = StatsState.serializer(),
            defaultValue = StatsState()
        )
    }

    // -- Initial state --

    @Test
    fun `initial read returns default empty StatsState`() = runTest {
        val store = createStoreManager(backgroundScope)

        val result = store.data.first()

        assertEquals(StatsState(), result)
        assertTrue(result.entries.isEmpty())
    }

    // -- Write then read round-trip --

    @Test
    fun `write then read preserves all ScoreEntry fields`() = runTest {
        val store = createStoreManager(backgroundScope)

        val entry = ScoreEntry(
            id = "abc-123",
            size = 8,
            timeMillis = 5000L,
            moves = 12,
            epochMillis = 1700000000000L
        )

        store.update { it.copy(entries = listOf(entry)) }

        val result = store.data.first()

        assertEquals(1, result.entries.size)
        val saved = result.entries.first()
        assertEquals("abc-123", saved.id)
        assertEquals(8, saved.size)
        assertEquals(5000L, saved.timeMillis)
        assertEquals(12, saved.moves)
        assertEquals(1700000000000L, saved.epochMillis)
    }

    // -- Sequential updates --

    @Test
    fun `sequential updates accumulate entries`() = runTest {
        val store = createStoreManager(backgroundScope)

        val entry1 = ScoreEntry("id-1", 4, 1000L, 4, 100L)
        val entry2 = ScoreEntry("id-2", 5, 2000L, 6, 200L)
        val entry3 = ScoreEntry("id-3", 6, 3000L, 8, 300L)

        store.update { it.copy(entries = it.entries + entry1) }
        store.update { it.copy(entries = it.entries + entry2) }
        store.update { it.copy(entries = it.entries + entry3) }

        val result = store.data.first()

        assertEquals(3, result.entries.size)
        assertEquals("id-1", result.entries[0].id)
        assertEquals("id-2", result.entries[1].id)
        assertEquals("id-3", result.entries[2].id)
    }

    // -- Concurrent updates --

    @Test
    fun `concurrent updates do not lose data`() = runTest {
        val store = createStoreManager(backgroundScope)
        val count = 20

        val jobs = (1..count).map { i ->
            async {
                store.update { state ->
                    val entry = ScoreEntry("id-$i", 4, i * 100L, i, i * 1000L)
                    state.copy(entries = state.entries + entry)
                }
            }
        }
        jobs.awaitAll()

        val result = store.data.first()

        assertEquals(
            "All $count concurrent updates should be persisted",
            count,
            result.entries.size
        )
    }

    // -- Flow reactivity --

    @Test
    fun `flow emits reactively on updates`() = runTest {
        val store = createStoreManager(backgroundScope)

        // Collect the first 3 emissions: initial + 2 updates
        val emissions = async {
            store.data.take(3).toList()
        }

        store.update { it.copy(entries = it.entries + ScoreEntry("a", 4, 100, 4, 100)) }
        store.update { it.copy(entries = it.entries + ScoreEntry("b", 5, 200, 5, 200)) }

        val results = emissions.await()

        assertEquals(3, results.size)
        assertEquals(0, results[0].entries.size)
        assertEquals(1, results[1].entries.size)
        assertEquals(2, results[2].entries.size)
    }

    // -- Large payload --

    @Test
    fun `large payload with 100 entries round-trips correctly`() = runTest {
        val store = createStoreManager(backgroundScope)

        val entries = (1..100).map { i ->
            ScoreEntry("id-$i", i % 20 + 4, i * 100L, i, i * 1000L)
        }

        store.update { it.copy(entries = entries) }

        val result = store.data.first()

        assertEquals(100, result.entries.size)
        assertEquals("id-1", result.entries.first().id)
        assertEquals("id-100", result.entries.last().id)
    }

    // -- Corrupted JSON recovery --

    @Test
    fun `corrupted JSON falls back to default value`() = runTest {
        val dir = createTempDirectory("datastore_corrupt").toFile()
        val file = File(dir, "corrupt_prefs.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(scope = backgroundScope) { file }

        // First, write valid data
        val validStore = StoreManagerImpl(
            dataStore = dataStore,
            key = testKey,
            serializer = StatsState.serializer(),
            defaultValue = StatsState()
        )
        validStore.update {
            it.copy(entries = listOf(ScoreEntry("valid", 4, 100, 4, 100)))
        }

        // Verify it was written
        val written = validStore.data.first()
        assertEquals(1, written.entries.size)

        // Now corrupt the data by writing invalid JSON directly
        dataStore.edit { prefs ->
            prefs[testKey] = "{invalid json that is definitely not parseable!!"
        }

        // Read should fall back to default
        val result = validStore.data.first()
        assertEquals(StatsState(), result)
        assertTrue(result.entries.isEmpty())
    }
}
