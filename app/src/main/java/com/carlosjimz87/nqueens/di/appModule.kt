package com.carlosjimz87.nqueens.di

import com.carlosjimz87.nqueens.presentation.board.viewmodel.BoardViewModel
import com.carlosjimz87.nqueens.presentation.timer.CoroutineGameTimer
import com.carlosjimz87.nqueens.presentation.timer.GameTimer
import com.carlosjimz87.rules.board.BoardChecker
import com.carlosjimz87.rules.board.BoardCheckerImpl
import com.carlosjimz87.rules.game.QueenConflictsChecker
import com.carlosjimz87.rules.game.QueenConflictsCheckerImpl
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    factory<GameTimer> { CoroutineGameTimer() }

    single<QueenConflictsChecker> { QueenConflictsCheckerImpl() }
    single<BoardChecker> { BoardCheckerImpl() }

    viewModel {
        BoardViewModel(
            get(),
            get(),
            get()
        )
    }
}