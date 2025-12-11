package com.carlosjimz87.nqueens.di

import com.carlosjimz87.nqueens.presentation.timer.CoroutineGameTimer
import com.carlosjimz87.nqueens.presentation.timer.GameTimer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object GameTimerModule {

    @ViewModelScoped
    @Provides
    fun provideGameTimer(): GameTimer = CoroutineGameTimer()
}