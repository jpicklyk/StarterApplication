package com.example.starterapplication.core.knox.feature.domain

import android.util.Log
import com.example.starterapplication.core.knox.api.ApiError
import com.example.starterapplication.core.knox.api.ApiResult
import com.example.starterapplication.core.knox.feature.domain.handler.KnoxFeatureHandlerFactory
import com.example.starterapplication.core.knox.feature.domain.model.KnoxFeature
import com.example.starterapplication.core.knox.feature.domain.model.KnoxFeatureKey
import com.example.starterapplication.core.knox.feature.domain.registry.KnoxFeatureCategory
import com.example.starterapplication.core.knox.feature.domain.registry.KnoxFeatureRegistry
import com.example.starterapplication.core.knox.feature.presentation.KnoxFeatureState
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

sealed class KnoxFeatureError : ApiError {
    data class OperationFailed(override val message: String) : KnoxFeatureError()
}

@Single
class KnoxFeatureManager(
    private val featureRegistry: KnoxFeatureRegistry
) : KoinComponent {
    private val tag = "KnoxFeatureManager"
    private val featureHandlerFactory: KnoxFeatureHandlerFactory by inject()

    suspend fun getFeatureState(feature: KnoxFeatureKey<*>): ApiResult<KnoxFeatureState<*>> {
        Log.d(tag, "getFeatureState: $feature")
        val handlerResult = featureHandlerFactory.getHandler(feature)
        Log.d(tag, "getFeatureState: handlerResult: $handlerResult")
        return when (handlerResult) {
            is ApiResult.Success -> handlerResult.data.getState()
            is ApiResult.Error -> ApiResult.Error(handlerResult.apiError)
            is ApiResult.NotSupported -> ApiResult.NotSupported
        }
    }

    suspend fun setFeatureState(feature: KnoxFeatureKey<*>, newState: KnoxFeatureState<*>): ApiResult<Unit> {
        Log.d(tag, "setFeatureState: $feature")
        val handlerResult = featureHandlerFactory.getHandler(feature)
        Log.d(tag, "setFeatureState: handlerResult: $handlerResult")
        return when (handlerResult) {
            is ApiResult.Success -> handlerResult.data.setState(newState as KnoxFeatureState<Any?>)
            is ApiResult.Error -> ApiResult.Error(handlerResult.apiError)
            is ApiResult.NotSupported -> ApiResult.NotSupported
        }
    }

    suspend fun getAllFeatures(category: KnoxFeatureCategory? = null): ApiResult<List<KnoxFeature<*>>> {
        Log.d(tag, "getAllFeatures: category: $category")
        return try {
            val features = featureRegistry.getFeatures(category).mapNotNull { featureKey ->
                Log.d(tag, "getAllFeatures: featureKey: $featureKey")
                val stateResult = getFeatureState(featureKey)
                Log.d(tag, "getAllFeatures: stateResult: $stateResult")
                when (stateResult) {
                    is ApiResult.Success -> KnoxFeature(featureKey, stateResult.data)
                    else -> null  // Skip features that couldn't be retrieved
                }
            }
            ApiResult.Success(features)
        } catch (e: Exception) {
            Log.e(tag, "getAllFeatures: ${e.message}")
            ApiResult.Error(apiError = KnoxFeatureError.OperationFailed("Failed to get features"), exception = e)
        }
    }

    suspend fun getAllCategorizedFeatures(): ApiResult<Map<KnoxFeatureCategory, List<KnoxFeature<*>>>> {
        Log.d(tag, "getAllCategorizedFeatures")
        return try {
            val categorizedFeatures = featureRegistry.getCategorizedFeatures().mapValues { (_, featureKeys) ->
                featureKeys.mapNotNull { featureKey ->
                    Log.d(tag, "getAllCategorizedFeatures: featureKey: $featureKey")
                    val stateResult = getFeatureState(featureKey)
                    Log.d(tag, "getAllCategorizedFeatures: stateResult: $stateResult")
                    when (stateResult) {
                        is ApiResult.Success -> KnoxFeature(featureKey, stateResult.data)
                        else -> null  // Skip features that couldn't be retrieved
                    }
                }
            }
            ApiResult.Success(categorizedFeatures)
        } catch (e: Exception) {
            Log.e(tag, "getAllCategorizedFeatures: ${e.message}")
            ApiResult.Error(apiError = KnoxFeatureError.OperationFailed("Failed to get categorized features"), exception = e)
        }
    }
}