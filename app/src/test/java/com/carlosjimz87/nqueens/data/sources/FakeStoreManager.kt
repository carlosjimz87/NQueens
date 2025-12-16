package com.carlosjimz87.nqueens.data.sources

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class FakeStoreManager<T>(initial: T) : StoreManager<T> {
    private val mutex = Mutex()
    private val state = MutableStateFlow(initial)

    override val data: Flow<T> = state

    override suspend fun update(block: (T) -> T) {
        mutex.withLock {
            state.value = block(state.value)
        }
    }

    fun current(): T = state.value
}