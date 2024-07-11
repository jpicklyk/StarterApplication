package com.example.starterapplication.core.knox.feature

import com.example.starterapplication.core.knox.api.ApiResult
import com.example.starterapplication.core.knox.feature.domain.KnoxFeatureError
import com.example.starterapplication.core.knox.feature.domain.handler.KnoxFeatureHandler
import com.example.starterapplication.core.knox.feature.domain.handler.KnoxFeatureHandlerFactory
import com.example.starterapplication.core.knox.feature.domain.model.KnoxFeatureKey
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FeatureHandlerFactoryTest : KoinTest {

    private val featureHandlerFactory: KnoxFeatureHandlerFactory by inject()

    @Before
    fun setup() {
        stopKoin()
        startKoin {
            modules(
                module {
                    single { KnoxFeatureHandlerFactory() }
                    single(qualifier = named("TestFeature")) { mockk<KnoxFeatureHandler<Boolean>>() }
                }
            )
        }
    }

    @Test
    fun `getHandler returns correct handler for existing feature`() {
        val testFeature = object : KnoxFeatureKey<Boolean> {
            override val featureName: String = "TestFeature"
        }

        val result = featureHandlerFactory.getHandler(testFeature)
        assertTrue(result is ApiResult.Success)
    }

    @Test
    fun `getHandler returns Error for non-existent feature`() {
        val nonExistentFeature = object : KnoxFeatureKey<String> {
            override val featureName: String = "NonExistentFeature"
        }

        val result = featureHandlerFactory.getHandler(nonExistentFeature)

        assertTrue(result is ApiResult.Error)
        assertTrue(result.apiError is KnoxFeatureError.OperationFailed)
        assertEquals(
            "No handler found for feature: NonExistentFeature",
            (result.apiError as KnoxFeatureError.OperationFailed).message
        )
    }
}