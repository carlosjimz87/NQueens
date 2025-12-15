package com.carlosjimz87.nqueens.store.manager

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

    override val data: Flow<T> = dataStore.data.map { prefs ->
        val raw = prefs[key] ?: return@map defaultValue
        runCatching { json.decodeFromString(serializer, raw) }.getOrDefault(defaultValue)
    }

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