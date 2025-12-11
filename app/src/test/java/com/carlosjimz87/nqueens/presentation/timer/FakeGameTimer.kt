package com.carlosjimz87.nqueens.presentation.timer

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

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

    // Helper to simulate time for tests si lo necesitas
    fun setElapsed(millis: Long) {
        _elapsedMillis.value = millis
    }
}