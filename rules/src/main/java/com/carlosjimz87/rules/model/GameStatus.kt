package com.carlosjimz87.rules.model

sealed interface GameStatus {

    data class NotStarted(
        val size: Int
    ) : GameStatus

    data class InProgress(
        val size: Int,
        val queensPlaced: Int,
        val conflicts: Int
    ) : GameStatus

    data class Solved(
        val size: Int,
        val moves: Int
    ) : GameStatus
}