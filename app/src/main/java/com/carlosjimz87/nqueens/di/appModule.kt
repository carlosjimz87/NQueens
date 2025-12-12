package com.carlosjimz87.nqueens.di

import com.carlosjimz87.nqueens.common.Constants
import com.carlosjimz87.nqueens.presentation.board.viewmodel.BoardViewModel
import com.carlosjimz87.nqueens.presentation.timer.CoroutineGameTimer
import com.carlosjimz87.nqueens.presentation.timer.GameTimer
import com.carlosjimz87.rules.QueenConflictsChecker
import com.carlosjimz87.rules.QueenConflictsCheckerImpl
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    factory<GameTimer> { CoroutineGameTimer() }

    single<QueenConflictsChecker> { QueenConflictsCheckerImpl() }

    viewModel {
        BoardViewModel(
            get(),
            get()
        )
    }
}