package com.carlosjimz87.nqueens.store.manager

import kotlinx.coroutines.flow.Flow

/**
 * Defines a generic contract for managing persistent data of a specific type [T].
 * See [StoreManagerImpl] for a concrete implementation.
 *
 * Implementations of this interface are responsible for providing a reactive way to
 * observe changes to the stored data and an atomic mechanism to update it.
 * This is often used for managing application preferences or user-specific data.
 *
 * @param T The type of data that this store manager will handle.
 */
interface StoreManager<T> {

    val data: Flow<T>
    suspend fun update(block: (T) -> T)
}