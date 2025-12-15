package com.carlosjimz87.nqueens.store.manager

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

private val Context.appDataStore by preferencesDataStore(name = "nqueens_store")

class StoreManager<T>(
    private val context: Context,
    private val key: Preferences.Key<String>,
    private val serializer: KSerializer<T>,
    private val defaultValue: T,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
    }
) {
    val data: Flow<T> = context.appDataStore.data.map { prefs ->
        val raw = prefs[key] ?: return@map defaultValue
        runCatching { json.decodeFromString(serializer, raw) }.getOrDefault(defaultValue)
    }

    suspend fun update(transform: (T) -> T) {
        context.appDataStore.edit { prefs ->
            val current = prefs[key]?.let {
                runCatching { json.decodeFromString(serializer, it) }.getOrNull()
            } ?: defaultValue

            val next = transform(current)
            prefs[key] = json.encodeToString(serializer, next)
        }
    }
}