package com.example.starterapplication.core.knox.feature

import com.example.starterapplication.core.knox.feature.domain.KnoxFeatureManager
import com.example.starterapplication.core.knox.feature.domain.handler.KnoxFeatureHandlerFactory
import com.example.starterapplication.core.knox.feature.domain.registry.KnoxFeatureRegistry
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.get
import org.koin.dsl.module
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class KoinInjectionTest : KoinTest {

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        modules(
            module {
                single { KnoxFeatureManager(get()) }
                single { KnoxFeatureHandlerFactory() }
                single { KnoxFeatureRegistry() }
                // Add other dependencies as needed
            }
        )
    }

    @Test
    fun `KnoxFeatureManager is injected`() {
        val manager: KnoxFeatureManager = get()
        assertNotNull(manager)
    }

    @Test
    fun `KnoxFeatureHandlerFactory is injected`() {
        val factory: KnoxFeatureHandlerFactory = get()
        assertNotNull(factory)
    }

    @Test
    fun `KnoxFeatureRegistry is injected`() {
        val registry: KnoxFeatureRegistry = get()
        assertNotNull(registry)
    }

}