package com.carlosjimz87.nqueens.common

import com.carlosjimz87.nqueens.store.model.Leaderboards
import com.carlosjimz87.nqueens.store.model.ScoreEntry

fun formatElapsed(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

// --- Preview helpers ---

fun scoreEntry(
    id: String = "preview-${java.util.UUID.randomUUID()}", // UUID required to prevent render issues in previews
    size: Int = 8,
    epochMillis: Long = 0L,
    timeMillis: Long = 60_000L,
    moves: Int = 30,
): ScoreEntry = ScoreEntry(
    id = id,
    size = size,
    epochMillis = epochMillis,
    timeMillis = timeMillis,
    moves = moves
)

fun previewLeaderboardsNormal(): Leaderboards {
    return Leaderboards(
        byTime = listOf(
            scoreEntry(timeMillis = 35_000L, moves = 22),
            scoreEntry(timeMillis = 51_000L, moves = 26),
            scoreEntry(timeMillis = 79_000L, moves = 31),
        ),
        byMoves = listOf(
            scoreEntry(timeMillis = 62_000L, moves = 18),
            scoreEntry(timeMillis = 41_000L, moves = 19),
            scoreEntry(timeMillis = 95_000L, moves = 20),
        )
    )
}

fun previewLeaderboardsStress(): Leaderboards {
    val many = (1..25).map { i ->
        scoreEntry(
            timeMillis = i * 97_531L,
            moves = i * 7 + (i % 3)
        )
    }
    return Leaderboards(
        byTime = many.sortedBy { it.timeMillis },
        byMoves = many.sortedBy { it.moves }
    )
}