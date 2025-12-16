package com.carlosjimz87.nqueens.data.sources

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.carlosjimz87.nqueens.data.model.ScoreEntry
import com.carlosjimz87.nqueens.data.model.StatsState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

@OptIn(ExperimentalCoroutinesApi::class)
class StoreManagerImplTest {

    @Test
    fun `data emits defaultValue when key not present`() = runTest {
        val ds = createPrefsDataStore(prefsFile("stats"))
        val key = stringPreferencesKey("stats")

        val manager = StoreManagerImpl(
            dataStore = ds,
            key = key,
            serializer = StatsState.serializer(),
            defaultValue = StatsState(entries = emptyList()),
            json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
        )

        val value = manager.data.first()
        assertEquals(0, value.entries.size)
    }

    @Test
    fun `update persists value and data emits it`() = runTest {
        val ds = createPrefsDataStore(prefsFile("stats"))
        val key = stringPreferencesKey("stats")

        val manager = StoreManagerImpl(
            dataStore = ds,
            key = key,
            serializer = StatsState.serializer(),
            defaultValue = StatsState(),
        )

        manager.update { it.copy(entries = it.entries + ScoreEntry("id", 8, 0L, 1000, 20)) }

        val saved = manager.data.first()
        assertEquals(1, saved.entries.size)
        assertEquals("id", saved.entries.first().id)
    }

    @Test
    fun `corrupted json falls back to defaultValue`() = runTest {
        val ds = createPrefsDataStore(prefsFile("stats"))
        val key = stringPreferencesKey("stats")

        // write corrupted payload directly
        ds.edit { it[key] = "{not json" }

        val manager = StoreManagerImpl(
            dataStore = ds,
            key = key,
            serializer = StatsState.serializer(),
            defaultValue = StatsState(entries = emptyList()),
        )

        val value = manager.data.first()
        assertEquals(0, value.entries.size)
    }

    @Test
    fun `update receives defaultValue when key is missing`() = runTest {
        val ds = createPrefsDataStore(prefsFile("stats_missing_in_update"))
        val key = stringPreferencesKey("stats")

        val default = StatsState(entries = emptyList())

        val manager = StoreManagerImpl(
            dataStore = ds,
            key = key,
            serializer = StatsState.serializer(),
            defaultValue = default,
            json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
        )

        var seenCurrent: StatsState? = null

        manager.update { current ->
            seenCurrent = current
            current // no-op
        }

        assertEquals(default, seenCurrent)
    }

    @Test
    fun `update receives decoded value when key exists with valid json`() = runTest {
        val ds = createPrefsDataStore(prefsFile("stats_valid_in_update"))
        val key = stringPreferencesKey("stats")

        val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

        val seeded = StatsState(
            entries = listOf(ScoreEntry(id = "seed", size = 8, epochMillis = 0L, timeMillis = 1111L, moves = 9))
        )

        // seed valid payload
        ds.edit { prefs ->
            prefs[key] = json.encodeToString(StatsState.serializer(), seeded)
        }

        val manager = StoreManagerImpl(
            dataStore = ds,
            key = key,
            serializer = StatsState.serializer(),
            defaultValue = StatsState(entries = emptyList()),
            json = json
        )

        var seenCurrent: StatsState? = null

        manager.update { current ->
            seenCurrent = current
            current // no-op
        }

        // If update decoded correctly, current must be the seeded object
        assertEquals(1, seenCurrent!!.entries.size)
        assertEquals("seed", seenCurrent!!.entries.first().id)
    }

    @Test
    fun `update falls back to defaultValue when key exists but json is corrupted`() = runTest {
        val ds = createPrefsDataStore(prefsFile("stats_corrupt_in_update"))
        val key = stringPreferencesKey("stats")

        // seed corrupted payload
        ds.edit { prefs -> prefs[key] = "{not json" }

        val default = StatsState(entries = emptyList())

        val manager = StoreManagerImpl(
            dataStore = ds,
            key = key,
            serializer = StatsState.serializer(),
            defaultValue = default,
            json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
        )

        var seenCurrent: StatsState? = null

        manager.update { current ->
            seenCurrent = current
            current // no-op
        }

        // This is the missing branch: decode fails inside update(), so it must fall back.
        assertEquals(default, seenCurrent)
    }

    fun prefsFile(name: String): File {
        val dir = createTempDirectory("ds_test_").toFile()
        return File(dir, "$name.preferences_pb")
    }

    fun TestScope.createPrefsDataStore(file: File): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { file }
        )
    }

}