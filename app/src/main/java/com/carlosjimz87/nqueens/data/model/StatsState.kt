package com.carlosjimz87.nqueens.data.model

import kotlinx.serialization.Serializable

@Serializable
data class StatsState(
    val entries: List<ScoreEntry> = emptyList()
)
