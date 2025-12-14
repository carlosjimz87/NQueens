package com.carlosjimz87.nqueens.presentation.timer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class CoroutineGameTimer(
    private val tickMillis: Long = 1000L
) : GameTimer {
    private val _elapsedMillis = MutableStateFlow(0L)
    override val elapsedMillis: StateFlow<Long> = _elapsedMillis

    private var job: Job? = null
    private var scope: CoroutineScope? = null

    override fun start() {
        if (job != null) return

        if (scope == null) {
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        }

        val localScope = scope!!

        job = localScope.launch {
            val base = System.currentTimeMillis() - _elapsedMillis.value
            while (isActive) {
                delay(tickMillis)
                _elapsedMillis.value = System.currentTimeMillis() - base
            }
        }
    }

    override fun stop() {
        job?.cancel()
        job = null
    }

    override fun reset() {
        _elapsedMillis.value = 0L
    }
}