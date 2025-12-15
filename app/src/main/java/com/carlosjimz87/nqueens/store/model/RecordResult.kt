package com.carlosjimz87.nqueens.store.model

data class RecordResult(
    val entry: ScoreEntry,
    val rankByTime: Int,
    val rankByMoves: Int
)