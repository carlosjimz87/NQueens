package com.carlosjimz87.nqueens.store.model

data class Leaderboards(
    val byTime: List<ScoreEntry>,
    val byMoves: List<ScoreEntry>,
)