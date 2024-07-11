package com.example.starterapplication.core.knox.api

import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestDispatcher
import org.junit.Before

open class BaseCoroutineTest {
    protected lateinit var testScheduler: TestCoroutineScheduler
    protected lateinit var testDispatcher: TestDispatcher

    @Before
    fun setUp() {
        testScheduler = TestCoroutineScheduler()
        testDispatcher = StandardTestDispatcher(testScheduler)
    }
}