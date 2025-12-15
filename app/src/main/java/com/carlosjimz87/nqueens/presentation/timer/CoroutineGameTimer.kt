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

/**
 * An implementation of [GameTimer] that uses Kotlin Coroutines to provide a ticking timer.
 *
 * This timer uses a [StateFlow] to emit elapsed time updates and manages its lifecycle
 * with a [CoroutineScope] and [Job].
 *
 * @param tickMillis The interval in milliseconds at which the timer's [elapsedMillis] will be updated.
 *                   Defaults to 1000ms (1 second).
 */
class CoroutineGameTimer(
    private val tickMillis: Long = 1000L
) : GameTimer {
    private val _elapsedMillis = MutableStateFlow(0L)
    override val elapsedMillis: StateFlow<Long> = _elapsedMillis

    /**
     * The [Job] that controls the lifecycle of the coroutine responsible for updating the timer.
     * It is `null` when the timer is stopped or has not yet been started.
     */
    private var job: Job? = null
    /**
     * The [CoroutineScope] within which the timer's update coroutine is launched.
     * It's initialized lazily upon the first call to [start].
     */
    private var scope: CoroutineScope? = null

    /**
     * Starts or resumes the game timer.
     * If the timer is already running, this call is ignored.
     * A new [CoroutineScope] is created if it doesn't already exist.
     * The coroutine launched will continuously update [elapsedMillis] at intervals defined by [tickMillis].
     */
    override fun start() {
        if (job != null) return // already running

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

    /**
     * Stops the timer by canceling the associated coroutine [job].
     */
    override fun stop() {
        job?.cancel()
        job = null
    }

    /**
     * Resets the timer to its initial state.
     */
    override fun reset() {
        stop()
        _elapsedMillis.value = 0L
    }
}