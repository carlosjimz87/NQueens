package com.carlosjimz87.nqueens.di

import com.carlosjimz87.nqueens.presentation.board.viewmodel.BoardViewModel
import com.carlosjimz87.nqueens.presentation.timer.FakeGameTimer
import com.carlosjimz87.nqueens.presentation.timer.GameTimer
import com.carlosjimz87.rules.solver.NQueensSolver
import com.carlosjimz87.rules.solver.NQueensSolverImpl
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val testModule = module {

    single<GameTimer> { FakeGameTimer() }

    single<NQueensSolver> { NQueensSolverImpl() }

    viewModel {
        BoardViewModel(
            get(),
            get(),
        )
    }
}