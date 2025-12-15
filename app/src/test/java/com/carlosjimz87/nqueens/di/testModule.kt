package com.carlosjimz87.nqueens.di

import com.carlosjimz87.nqueens.presentation.board.viewmodel.BoardViewModel
import com.carlosjimz87.nqueens.presentation.timer.FakeGameTimer
import com.carlosjimz87.nqueens.presentation.timer.GameTimer
import com.carlosjimz87.nqueens.store.manager.FakeStoreManager
import com.carlosjimz87.nqueens.store.manager.StoreManager
import com.carlosjimz87.nqueens.store.model.StatsState
import com.carlosjimz87.nqueens.store.repo.StatsRepository
import com.carlosjimz87.nqueens.store.repo.StatsRepositoryImpl
import com.carlosjimz87.rules.solver.NQueensSolver
import com.carlosjimz87.rules.solver.NQueensSolverImpl
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val testModule = module(override = true) {

    // Fake Game Timer
    single<GameTimer> { FakeGameTimer() }

    // Real solver
    single<NQueensSolver> { NQueensSolverImpl() }

    // Fake Store for StatsState
    single<StoreManager<StatsState>> { FakeStoreManager(StatsState()) }

    // Real repository
    single<StatsRepository> { StatsRepositoryImpl(store = get()) }

    viewModel {
        BoardViewModel(
            timer = get(),
            solver = get(),
            statsRepo = get(),
            initialSize = 8 // to avoid null size issues in tests due to a missing board size
        )
    }
}