package com.carlosjimz87.nqueens.presentation.timer

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * A fake implementation of [GameTimer] for testing purposes.
 *
 * This class allows for manual control over the timer's state and elapsed time,
 * and tracks the number of times its methods ([start], [stop], [reset]) are called.
 * This is useful for verifying interactions with the timer in unit and integration tests
 * without depending on real-time delays.
 *
 * @property elapsedMillis A [StateFlow] that emits the current elapsed time in milliseconds.
 * @property startCalled The number of times the [start] method has been called.
 * @property stopCalled The number of times the [stop] method has been called.
 * @property resetCalled The number of times the [reset] method has been called.
 */
class FakeGameTimer : GameTimer {

    private val _elapsedMillis = MutableStateFlow(0L)
    override val elapsedMillis: StateFlow<Long> = _elapsedMillis

    var startCalled = 0
    var stopCalled = 0
    var resetCalled = 0

    override fun start() {
        startCalled++
    }

    override fun stop() {
        stopCalled++
    }

    override fun reset() {
        resetCalled++
        _elapsedMillis.value = 0L
    }

    fun setElapsed(millis: Long) {
        _elapsedMillis.value = millis
    }
}