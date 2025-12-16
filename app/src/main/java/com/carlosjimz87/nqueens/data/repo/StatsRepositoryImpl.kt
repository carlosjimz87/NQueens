package com.carlosjimz87.nqueens.data.repo

import com.carlosjimz87.nqueens.data.sources.StoreManager
import com.carlosjimz87.nqueens.data.model.Leaderboards
import com.carlosjimz87.nqueens.data.model.RecordResult
import com.carlosjimz87.nqueens.data.model.ScoreEntry
import com.carlosjimz87.nqueens.data.model.StatsState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * Implementation of the [StatsRepository] interface.
 *
 * This class is responsible for managing game statistics, including recording new scores
 * and retrieving leaderboards. It leverages a [StoreManager] to persist [StatsState] data.
 *
 * @param store The [StoreManager] instance used for data persistence, holding the [StatsState].
 */
class StatsRepositoryImpl(
    private val store: StoreManager<StatsState>
) : StatsRepository {

    /**
     * Exposes the current [StatsState] as a [Flow].
     * Any changes to the stored statistics will be emitted through this flow.
     */
    override val stats: Flow<StatsState> = store.data

    /**
     * Generates and returns leaderboards for a specific board size.
     * The leaderboards are sorted by time (best time first) and by moves (least moves first).
     *
     * @param size The size of the N-Queens board for which to retrieve leaderboards.
     * @param limit The maximum number of entries to return in each leaderboard list.
     * @return A [Flow] emitting [Leaderboards] containing two lists of [ScoreEntry] objects:
     *         one sorted by time and one sorted by moves.
     */
    override fun leaderboards(size: Int, limit: Int): Flow<Leaderboards> {
        return stats.map { state ->
            val (byTime, byMoves) = getSortedScores(state.entries, size, limit)
            Leaderboards(
                byTime = byTime,
                byMoves = byMoves,
            )
        }
    }

    /**
     * Records a new game score.
     *
     * If an existing score for the same board size and number of moves is found
     * and the new score's time is better, the existing score is updated. Otherwise, a new entry is added.
     * The leaderboards are then re-calculated to determine the rank of the new entry.
     *
     * @param size The size of the N-Queens board.
     * @param timeMillis The time taken to solve the puzzle in milliseconds.
     * @param moves The number of moves made to solve the puzzle.
     * @param limit The maximum number of entries to consider for ranking in the leaderboards.
     * @return A [RecordResult] containing the new or updated [ScoreEntry] and its ranks
     *         in the time-based and moves-based leaderboards.
     */
    override suspend fun record(size: Int, timeMillis: Long, moves: Int, limit: Int): RecordResult {
        val newEntry = ScoreEntry(
            id = UUID.randomUUID().toString(),
            size = size,
            timeMillis = timeMillis,
            moves = moves,
            epochMillis = System.currentTimeMillis()
        )

        var rankTime = 0
        var rankMoves = 0

        store.update { state ->
            val existing = state.entries.firstOrNull { it.size == size && it.moves == moves }
            val next = when {
                existing == null -> state.entries + newEntry
                timeMillis < existing.timeMillis -> state.entries - existing + newEntry
                else -> state.entries
            }

            val (byTime, byMoves) = getSortedScores(next, size, limit)

            rankTime = byTime.indexOfFirst { it.id == newEntry.id }.let { if (it >= 0) it + 1 else 0 }
            rankMoves = byMoves.indexOfFirst { it.id == newEntry.id }.let { if (it >= 0) it + 1 else 0 }

            state.copy(entries = next)
        }

        return RecordResult(newEntry, rankTime, rankMoves)
    }

    /**
     * A private helper function to retrieve sorted lists of [ScoreEntry] objects
     * based on time and moves for a given board size.
     * This function is crucial for reducing code duplication in leaderboard generation.
     *
     * @param entries The full list of [ScoreEntry] objects to filter and sort from.
     * @param size The board size to filter the entries by.
     * @param limit The maximum number of top scores to take for each sorted list.
     * @return A [Pair] where the first element is a list of scores sorted by time (ascending)
     *         and the second element is a list of scores sorted by moves (ascending).
     */
    private fun getSortedScores(entries: List<ScoreEntry>, size: Int, limit: Int): Pair<List<ScoreEntry>, List<ScoreEntry>> {
        val scoped = entries.filter { it.size == size }
        val byTime = scoped.sortedWith(compareBy<ScoreEntry> { it.timeMillis }.thenBy { it.moves }).take(limit)
        val byMoves = scoped.sortedWith(compareBy<ScoreEntry> { it.moves }.thenBy { it.timeMillis }).take(limit)
        return Pair(byTime, byMoves)
    }
}