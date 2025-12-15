package com.carlosjimz87.nqueens.store.repo

import com.carlosjimz87.nqueens.store.model.Leaderboards
import com.carlosjimz87.nqueens.store.model.RecordResult
import com.carlosjimz87.nqueens.store.model.StatsState
import kotlinx.coroutines.flow.Flow

interface StatsRepository {
    val stats: Flow<StatsState>
    fun leaderboards(size: Int, limit: Int = 10): Flow<Leaderboards>
    suspend fun record(size: Int, timeMillis: Long, moves: Int, limit: Int = 10): RecordResult
}