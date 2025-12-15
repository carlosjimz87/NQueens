package com.carlosjimz87.nqueens.store.repo

import com.carlosjimz87.nqueens.store.manager.StoreManager
import com.carlosjimz87.nqueens.store.model.Leaderboards
import com.carlosjimz87.nqueens.store.model.RecordResult
import com.carlosjimz87.nqueens.store.model.ScoreEntry
import com.carlosjimz87.nqueens.store.model.StatsState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class StatsRepositoryImpl(
    private val store: StoreManager<StatsState>
) : StatsRepository {

    override val stats: Flow<StatsState> = store.data

    override fun leaderboards(size: Int, limit: Int): Flow<Leaderboards> {
        return stats.map { state ->
            val scoped = state.entries.filter { it.size == size }
            Leaderboards(
                byTime = scoped.sortedWith(compareBy<ScoreEntry> { it.timeMillis }.thenBy { it.moves }).take(limit),
                byMoves = scoped.sortedWith(compareBy<ScoreEntry> { it.moves }.thenBy { it.timeMillis }).take(limit),
            )
        }
    }

    override suspend fun record(size: Int, timeMillis: Long, moves: Int, limit: Int): RecordResult {
        val newEntry = ScoreEntry(
            id = UUID.randomUUID().toString(),
            size = size,
            timeMillis = timeMillis,
            moves = moves,
            epochMillis = System.currentTimeMillis()
        )

        val rankTime = 0
        val rankMoves = 0

        store.update { state ->
            val existing = state.entries.firstOrNull { it.size == size && it.moves == moves }
            val next = when {
                existing == null -> state.entries + newEntry
                timeMillis < existing.timeMillis -> state.entries - existing + newEntry
                else -> state.entries // worse or equal -> ignore
            }

            // compute ranks from `next` (scoped = next.filter { it.size == size }) etc...
            state.copy(entries = next)
        }

        return RecordResult(newEntry, rankTime, rankMoves)
    }
}