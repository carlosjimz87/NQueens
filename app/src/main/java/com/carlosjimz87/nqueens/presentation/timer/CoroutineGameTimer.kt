package com.carlosjimz87.nqueens.presentation.timer

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class CoroutineGameTimer(
    private val scope: CoroutineScope,
    private val tickMillis: Long = 1000L
) : GameTimer {

    private val _elapsedMillis = MutableStateFlow(0L)
    override val elapsedMillis: StateFlow<Long> = _elapsedMillis

    private var job: Job? = null

    override fun start() {
        Log.d("CoroutineGameTimer", "start called")
        if (job != null) return // already running

        job = scope.launch {
            val base = System.currentTimeMillis() - _elapsedMillis.value
            while (isActive) {
                delay(tickMillis)
                _elapsedMillis.value = System.currentTimeMillis() - base
                Log.d("CoroutineGameTimer", "elapsedMillis updated: ${_elapsedMillis.value}")
            }
        }
    }

    override fun stop() {
        Log.d("CoroutineGameTimer", "stop called")
        job?.cancel()
        job = null
    }

    override fun reset() {
        Log.d("CoroutineGameTimer", "reset called")
        stop()
        _elapsedMillis.value = 0L
    }
}