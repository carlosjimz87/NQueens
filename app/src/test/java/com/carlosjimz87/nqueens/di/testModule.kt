package com.carlosjimz87.nqueens.di

import com.carlosjimz87.nqueens.presentation.board.viewmodel.BoardViewModel
import com.carlosjimz87.nqueens.presentation.rules.FakeQueenConflictsChecker
import com.carlosjimz87.nqueens.presentation.timer.FakeGameTimer
import com.carlosjimz87.nqueens.presentation.timer.GameTimer
import com.carlosjimz87.rules.QueenConflictsChecker
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val testModule = module {

    single<GameTimer> { FakeGameTimer() }

    single<QueenConflictsChecker> { FakeQueenConflictsChecker() }

    viewModel {
        BoardViewModel(
            get(),
            get()
        )
    }
}