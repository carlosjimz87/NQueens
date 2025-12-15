package com.carlosjimz87.nqueens.store.manager

import kotlinx.coroutines.flow.Flow

interface StoreManager<T> {
    val data: Flow<T>
    suspend fun update(block: (T) -> T)
}