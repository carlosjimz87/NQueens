package com.carlosjimz87.nqueens.data.model

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class StatsStateSerializationTest {

    private val json = Json { ignoreUnknownKeys = false }
    private val lenientJson = Json { ignoreUnknownKeys = true }

    private fun scoreEntry(
        id: String = "abc-123",
        size: Int = 8,
        timeMillis: Long = 5_000L,
        moves: Int = 12,
        epochMillis: Long = 1_700_000_000_000L,
    ) = ScoreEntry(
        id = id,
        size = size,
        timeMillis = timeMillis,
        moves = moves,
        epochMillis = epochMillis,
    )

    @Test
    fun `empty StatsState round-trips correctly`() {
        val original = StatsState()

        val serialized = json.encodeToString(StatsState.serializer(), original)
        val deserialized = json.decodeFromString(StatsState.serializer(), serialized)

        assertEquals(original, deserialized)
        assertEquals(emptyList<ScoreEntry>(), deserialized.entries)
    }

    @Test
    fun `StatsState with entries round-trips correctly`() {
        val original = StatsState(
            entries = listOf(
                scoreEntry(id = "entry-1", size = 4, moves = 6),
                scoreEntry(id = "entry-2", size = 12, moves = 20),
            )
        )

        val serialized = json.encodeToString(StatsState.serializer(), original)
        val deserialized = json.decodeFromString(StatsState.serializer(), serialized)

        assertEquals(original, deserialized)
        assertEquals(2, deserialized.entries.size)
    }

    @Test
    fun `ScoreEntry preserves all fields through serialization`() {
        val original = ScoreEntry(
            id = "unique-id-999",
            size = 16,
            timeMillis = 123_456L,
            moves = 42,
            epochMillis = 1_710_000_000_000L,
        )

        val serialized = json.encodeToString(ScoreEntry.serializer(), original)
        val deserialized = json.decodeFromString(ScoreEntry.serializer(), serialized)

        assertEquals(original.id, deserialized.id)
        assertEquals(original.size, deserialized.size)
        assertEquals(original.timeMillis, deserialized.timeMillis)
        assertEquals(original.moves, deserialized.moves)
        assertEquals(original.epochMillis, deserialized.epochMillis)
    }

    @Test
    fun `deserialization with unknown keys succeeds when ignoreUnknownKeys is true`() {
        val jsonString = """
            {
                "entries": [],
                "extraField": "should be ignored"
            }
        """.trimIndent()

        val result = lenientJson.decodeFromString(StatsState.serializer(), jsonString)

        assertEquals(StatsState(), result)
    }

    @Test
    fun `default values apply when entries field is missing from JSON`() {
        val jsonString = "{}"

        val result = json.decodeFromString(StatsState.serializer(), jsonString)

        assertEquals(emptyList<ScoreEntry>(), result.entries)
    }

    @Test
    fun `multiple entries maintain order after round-trip`() {
        val entries = (1..5).map { i ->
            scoreEntry(id = "id-$i", size = i, moves = i * 2, timeMillis = i * 1_000L)
        }
        val original = StatsState(entries = entries)

        val serialized = json.encodeToString(StatsState.serializer(), original)
        val deserialized = json.decodeFromString(StatsState.serializer(), serialized)

        assertEquals(entries.map { it.id }, deserialized.entries.map { it.id })
        assertEquals(entries, deserialized.entries)
    }
}
