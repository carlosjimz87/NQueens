package com.carlosjimz87.nqueens.store.model

import kotlinx.serialization.Serializable

@Serializable
data class ScoreEntry(
    val id: String,              // UUID
    val size: Int,
    val timeMillis: Long,
    val moves: Int,
    val epochMillis: Long
)