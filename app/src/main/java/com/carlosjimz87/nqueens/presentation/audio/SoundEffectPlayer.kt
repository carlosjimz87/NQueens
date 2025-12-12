package com.carlosjimz87.nqueens.presentation.audio

interface SoundEffectPlayer {
    fun playQueenPlaced()
    fun playQueenRemoved()
    fun playSolved()
    fun playStart()
}