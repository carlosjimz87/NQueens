package com.carlosjimz87.nqueens.common

import androidx.datastore.preferences.core.stringPreferencesKey

object Constants {
    const val DEFAULT_COLUMNS_ROWS = 4
    val STATS_KEY = stringPreferencesKey("stats_state_json")
    val APP_STORE_KEY = "nqueens_store"
}