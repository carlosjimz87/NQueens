package com.carlosjimz87.nqueens.presentation.timer

import kotlinx.coroutines.flow.StateFlow

interface GameTimer {
    val elapsedMillis: StateFlow<Long>

    fun start()
    fun stop()
    fun reset()
}