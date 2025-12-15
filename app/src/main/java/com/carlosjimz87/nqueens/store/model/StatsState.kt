package com.carlosjimz87.nqueens.store.model

import kotlinx.serialization.Serializable

@Serializable
data class StatsState(
    val entries: List<ScoreEntry> = emptyList()
)
