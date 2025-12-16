package com.carlosjimz87.nqueens.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.carlosjimz87.nqueens.common.Constants.STATS_KEY
import com.carlosjimz87.nqueens.presentation.board.viewmodel.BoardViewModel
import com.carlosjimz87.nqueens.presentation.timer.CoroutineGameTimer
import com.carlosjimz87.nqueens.presentation.timer.GameTimer
import com.carlosjimz87.nqueens.data.sources.StoreManager
import com.carlosjimz87.nqueens.data.sources.StoreManagerImpl
import com.carlosjimz87.nqueens.data.sources.appDataStore
import com.carlosjimz87.nqueens.data.model.StatsState
import com.carlosjimz87.nqueens.data.repo.StatsRepository
import com.carlosjimz87.nqueens.data.repo.StatsRepositoryImpl
import com.carlosjimz87.rules.solver.NQueensSolver
import com.carlosjimz87.rules.solver.NQueensSolverImpl
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    // Game Timer
    factory<GameTimer> { CoroutineGameTimer() }

    // Solver (holds in-memory state)
    single<NQueensSolver> { NQueensSolverImpl() }

    // Json instance
    single {
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            prettyPrint = false
        }
    }

    single<DataStore<Preferences>> { androidContext().appDataStore }

    // Store for StatsState in DataStore
    single<StoreManager<StatsState>> {
        StoreManagerImpl(
            dataStore = get(),
            key = STATS_KEY,
            serializer = StatsState.serializer(),
            defaultValue = StatsState(),
            json = get()
        )
    }
    // Repository
    single<StatsRepository> { StatsRepositoryImpl(store = get()) }

    // VM
    viewModel {
        BoardViewModel(
            timer = get(),
            solver = get(),
            statsRepo = get()
        )
    }
}