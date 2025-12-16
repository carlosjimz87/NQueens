package com.carlosjimz87.nqueens.data.model

data class Leaderboards(
    val byTime: List<ScoreEntry>,
    val byMoves: List<ScoreEntry>,
)