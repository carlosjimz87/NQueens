package com.carlosjimz87.nqueens

import android.app.Application
import com.carlosjimz87.nqueens.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class NQueensApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@NQueensApp)
            modules(appModule)
        }
    }
}