package com.carlosjimz87.nqueens.store.repo

import com.carlosjimz87.nqueens.store.model.Leaderboards
import com.carlosjimz87.nqueens.store.model.RecordResult
import com.carlosjimz87.nqueens.store.model.StatsState
import kotlinx.coroutines.flow.Flow

/**
 * Interface for accessing and managing game statistics and leaderboards.
 * See [StatsRepositoryImpl] for the implementation.
 *
 * This repository provides methods to observe overall game statistics,
 * fetch leaderboards for specific board sizes, and record new game results.
 */
interface StatsRepository {
    val stats: Flow<StatsState>
    fun leaderboards(size: Int, limit: Int = 10): Flow<Leaderboards>
    suspend fun record(size: Int, timeMillis: Long, moves: Int, limit: Int = 10): RecordResult
}