package com.carlosjimz87.nqueens.data.sources

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.carlosjimz87.nqueens.common.Constants.APP_STORE_KEY
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

val Context.appDataStore by preferencesDataStore(name = APP_STORE_KEY)

/**
 * An implementation of [StoreManager] that uses Jetpack DataStore ([Preferences])
 * and Kotlin Serialization to persist and retrieve data of type [T].
 *
 * This class provides a generic way to handle structured data persistence, automatically
 * serializing objects to JSON strings for storage and deserializing them back.
 *
 * @param T The type of data this manager will handle.
 * @param dataStore The [DataStore] instance where the data will be stored.
 * @param key The [Preferences.Key] used to identify and retrieve the serialized data in the DataStore.
 * @param serializer The [KSerializer] for type [T], used for encoding and decoding objects.
 * @param defaultValue The default value of type [T] to be used if no data is found or deserialization fails.
 * @param json The [Json] instance from Kotlinx Serialization used for JSON encoding and decoding.
 */
class StoreManagerImpl<T>(
    private val dataStore: DataStore<Preferences>,
    private val key: Preferences.Key<String>,
    private val serializer: KSerializer<T>,
    private val defaultValue: T,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
    }
) : StoreManager<T>{

    /**
     * A [Flow] that provides access to the stored data of type [T].
     * When collected, it reads the data from [dataStore], decodes it using the provided [serializer],
     * and falls back to [defaultValue] if the key is not found or deserialization fails.
     */
    override val data: Flow<T> = dataStore.data.map { prefs ->
        val raw = prefs[key] ?: return@map defaultValue
        runCatching { json.decodeFromString(serializer, raw) }.getOrDefault(defaultValue)
    }

    /**
     * Atomically updates the stored data in the [DataStore].
     * The provided `block` is executed with the current data state, and its result is
     * serialized and saved back to the DataStore. This operation handles deserialization
     * and serialization within a safe `edit` block.
     *
     * @param block A lambda that takes the current data state [T] and returns the new data state [T].
     *              The current state will be [defaultValue] if no data or decoding failure happens.
     */
    override suspend fun update(block: (T) -> T) {
        dataStore.edit { prefs ->
            val current = prefs[key]?.let {
                runCatching { json.decodeFromString(serializer, it) }.getOrNull()
            } ?: defaultValue

            val next = block(current)
            prefs[key] = json.encodeToString(serializer, next)
        }
    }
}