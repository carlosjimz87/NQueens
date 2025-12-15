package com.carlosjimz87.nqueens.di

import com.carlosjimz87.nqueens.common.Constants.STATS_KEY
import com.carlosjimz87.nqueens.presentation.board.viewmodel.BoardViewModel
import com.carlosjimz87.nqueens.presentation.timer.CoroutineGameTimer
import com.carlosjimz87.nqueens.presentation.timer.GameTimer
import com.carlosjimz87.nqueens.store.manager.StoreManager
import com.carlosjimz87.nqueens.store.model.StatsState
import com.carlosjimz87.nqueens.store.repo.StatsRepository
import com.carlosjimz87.nqueens.store.repo.StatsRepositoryImpl
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

    // Store for StatsState in DataStore
    single<StoreManager<StatsState>> {
        StoreManager(
            context = androidContext(),
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