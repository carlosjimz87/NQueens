package com.carlosjimz87.nqueens.presentation.audio

/**
 * Defines the contract for playing various sound effects within the N-Queens game.
 * See [AndroidSoundEffectPlayer] for concrete implementation.
 */
interface SoundEffectPlayer {
    fun playQueenPlaced()
    fun playQueenRemoved()
    fun playSolved()
    fun playStart()
    fun playConflict()
}