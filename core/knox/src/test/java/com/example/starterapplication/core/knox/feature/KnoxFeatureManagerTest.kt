package com.example.starterapplication.core.knox.feature

import com.example.starterapplication.core.knox.api.ApiResult
import com.example.starterapplication.core.knox.feature.domain.KnoxFeatureManager
import com.example.starterapplication.core.knox.feature.domain.handler.KnoxFeatureHandler
import com.example.starterapplication.core.knox.feature.domain.handler.KnoxFeatureHandlerFactory
import com.example.starterapplication.core.knox.feature.domain.model.KnoxFeature
import com.example.starterapplication.core.knox.feature.domain.model.KnoxFeatureKey
import com.example.starterapplication.core.knox.feature.domain.registry.KnoxFeatureCategory
import com.example.starterapplication.core.knox.feature.domain.registry.KnoxFeatureRegistry
import com.example.starterapplication.core.knox.feature.presentation.KnoxFeatureState
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkClass
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.mock.MockProviderRule
import org.koin.test.mock.declareMock
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KnoxFeatureManagerTest : KoinTest {

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        modules(
            module {
                single { KnoxFeatureManager(get()) }
                single { KnoxFeatureHandlerFactory() }
                single { KnoxFeatureRegistry() }
            }
        )
    }

    @get:Rule
    val mockProvider = MockProviderRule.create { clazz ->
        mockkClass(clazz)
    }

    private lateinit var knoxFeatureManager: KnoxFeatureManager
    private lateinit var featureHandlerFactory: KnoxFeatureHandlerFactory
    private lateinit var featureRegistry: KnoxFeatureRegistry

    @Before
    fun setup() {
        knoxFeatureManager = declareMock()
        featureHandlerFactory = declareMock()
        featureRegistry = declareMock()
    }

    @Test
    fun `getFeatureState returns success for existing feature`() = runBlocking {
        val testFeature = mockk<KnoxFeatureKey<Boolean>> {
            every { featureName } returns "TestFeature"
        }
        val featureHandler = mockk<KnoxFeatureHandler<Boolean>>()
        val featureState = KnoxFeatureState(
            enabled = true,
            value = true
        )

        coEvery { featureHandlerFactory.getHandler(testFeature) } returns ApiResult.Success(featureHandler)
        coEvery { featureHandler.getState() } returns ApiResult.Success(featureState)
        coEvery { knoxFeatureManager.getFeatureState(testFeature) } returns ApiResult.Success(featureState)


        val result = knoxFeatureManager.getFeatureState(testFeature)

        assertTrue(result is ApiResult.Success, "Expected Success, but got ${result::class.simpleName}")
        assertEquals(featureState, (result as ApiResult.Success).data)
    }

    @Test
    fun `setFeatureState returns success for valid state`() = runBlocking {
        val testFeature = mockk<KnoxFeatureKey<Boolean>> {
            every { featureName } returns "TestFeature"
        }
        val newState = KnoxFeatureState(enabled = false, value = false)

        coEvery { knoxFeatureManager.setFeatureState(testFeature, newState) } returns ApiResult.Success(Unit)

        val result = knoxFeatureManager.setFeatureState(testFeature, newState)

        assertTrue(result is ApiResult.Success, "Expected Success, but got ${result::class.simpleName}")
    }

    @Test
    fun `getAllFeatures returns list of features`() = runBlocking {
        val feature1 = mockk<KnoxFeatureKey<Boolean>> {
            every { featureName } returns "Feature1"
        }
        val feature2 = mockk<KnoxFeatureKey<String>> {
            every { featureName } returns "Feature2"
        }
        val featureList = listOf(
            KnoxFeature(feature1, KnoxFeatureState(enabled = true, value = true)),
            KnoxFeature(feature2, KnoxFeatureState(enabled = false, value = "disabled"))
        )

        coEvery { knoxFeatureManager.getAllFeatures() } returns ApiResult.Success(featureList)

        val result = knoxFeatureManager.getAllFeatures()

        assertTrue(result is ApiResult.Success, "Expected Success, but got ${result::class.simpleName}")
        assertEquals(featureList, (result as ApiResult.Success).data)
    }

    @Test
    fun `getAllCategorizedFeatures returns features grouped by category`() = runBlocking {
        val feature1 = mockk<KnoxFeatureKey<Boolean>> {
            every { featureName } returns "Feature1"
        }
        val feature2 = mockk<KnoxFeatureKey<String>> {
            every { featureName } returns "Feature2"
        }
        val categorizedFeatures = mapOf(
            KnoxFeatureCategory.PRODUCTION to listOf(
                KnoxFeature(feature1, KnoxFeatureState(enabled = true, value = true))
            ),
            KnoxFeatureCategory.DEPRECATED to listOf(
                KnoxFeature(feature2, KnoxFeatureState(enabled = false, value = "disabled"))
            )
        )

        coEvery { knoxFeatureManager.getAllCategorizedFeatures() } returns ApiResult.Success(categorizedFeatures)

        val result = knoxFeatureManager.getAllCategorizedFeatures()

        assertTrue(result is ApiResult.Success, "Expected Success, but got ${result::class.simpleName}")
        assertEquals(categorizedFeatures, (result as ApiResult.Success).data)
    }
}