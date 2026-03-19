package com.carlosjimz87.nqueens.data.sources

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FakeStoreManagerTest {

    @Test
    fun `current returns initial value`() {
        val store = FakeStoreManager("hello")

        assertEquals("hello", store.current())
    }

    @Test
    fun `data emits initial value`() = runTest {
        val store = FakeStoreManager("hello")

        val emitted = store.data.first()

        assertEquals("hello", emitted)
    }

    @Test
    fun `update changes current value`() = runTest {
        val store = FakeStoreManager(0)

        store.update { it + 1 }

        assertEquals(1, store.current())
    }

    @Test
    fun `data flow reflects updates`() = runTest {
        val store = FakeStoreManager(0)

        store.update { it + 1 }

        val emitted = store.data.first()
        assertEquals(1, emitted)
    }

    @Test
    fun `multiple sequential updates accumulate`() = runTest {
        val store = FakeStoreManager(0)

        store.update { it + 1 }
        store.update { it + 1 }
        store.update { it + 1 }

        assertEquals(3, store.current())
    }

    @Test
    fun `update block receives current value`() = runTest {
        val store = FakeStoreManager(10)

        store.update { it * 2 }

        assertEquals(20, store.current())
    }
}
