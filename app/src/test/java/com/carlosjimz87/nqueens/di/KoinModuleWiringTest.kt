package com.carlosjimz87.nqueens.di

import com.carlosjimz87.nqueens.MainDispatcherRule
import com.carlosjimz87.nqueens.data.repo.StatsRepository
import com.carlosjimz87.nqueens.data.sources.StoreManager
import com.carlosjimz87.nqueens.presentation.board.viewmodel.BoardViewModel
import com.carlosjimz87.nqueens.presentation.timer.GameTimer
import com.carlosjimz87.rules.solver.NQueensSolver
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.context.GlobalContext.stopKoin
import org.koin.java.KoinJavaComponent.getKoin

/**
 * Verifies that the Koin [testModule] resolves all bindings without crashing.
 *
 * This uses the [testModule] (which substitutes fakes for Android-bound dependencies
 * like DataStore) to verify that the dependency graph is wired correctly.
 *
 * Note: We test the [testModule] rather than [appModule] because [appModule] requires
 * an Android Context for DataStore, which is unavailable in JVM tests.
 * The testModule mirrors the production graph structure with fake substitutions.
 */
class KoinModuleWiringTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Before
    fun setUp() {
        startKoin { modules(testModule) }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `testModule resolves GameTimer binding`() {
        val timer = getKoin().get<GameTimer>()
        assertNotNull(timer)
    }

    @Test
    fun `testModule resolves NQueensSolver binding`() {
        val solver = getKoin().get<NQueensSolver>()
        assertNotNull(solver)
    }

    @Test
    fun `testModule resolves StoreManager binding`() {
        val store = getKoin().get<StoreManager<*>>()
        assertNotNull(store)
    }

    @Test
    fun `testModule resolves StatsRepository binding`() {
        val repo = getKoin().get<StatsRepository>()
        assertNotNull(repo)
    }

    @Test
    fun `testModule resolves BoardViewModel binding`() {
        val vm = getKoin().get<BoardViewModel>()
        assertNotNull(vm)
    }

    @Test
    fun `singleton bindings return same instance on repeated get`() {
        val koin = getKoin()

        val solver1 = koin.get<NQueensSolver>()
        val solver2 = koin.get<NQueensSolver>()
        assertTrue("NQueensSolver should be a singleton", solver1 === solver2)

        val repo1 = koin.get<StatsRepository>()
        val repo2 = koin.get<StatsRepository>()
        assertTrue("StatsRepository should be a singleton", repo1 === repo2)
    }
}
