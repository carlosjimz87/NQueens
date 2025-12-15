package com.carlosjimz87.nqueens.presentation.timer

import kotlinx.coroutines.flow.StateFlow
import com.carlosjimz87.nqueens.presentation.timer.CoroutineGameTimer

/**
 * Defines the contract for a game timer. See [CoroutineGameTimer] for a concrete implementation.
 *
 * Implementations of this interface should provide functionality to start, stop, and reset
 * a timer, as well as expose the current elapsed time as a [StateFlow].
 */
interface GameTimer {
    val elapsedMillis: StateFlow<Long>
    fun start()
    fun stop()
    fun reset()
}