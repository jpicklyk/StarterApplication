@file:Suppress("TYPEALIAS_EXPANSION_DEPRECATION")

package com.example.starterapplication.core.knox.feature.domain.handler

import com.example.starterapplication.core.knox.api.ApiResult
import com.example.starterapplication.core.knox.feature.domain.KnoxFeatureError
import com.example.starterapplication.core.knox.feature.domain.model.KnoxFeatureKey
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.koin.core.error.NoDefinitionFoundException
import org.koin.core.qualifier.named

@Single
class KnoxFeatureHandlerFactory : KoinComponent {
    fun <T> getHandler(feature: KnoxFeatureKey<T>): ApiResult<KnoxFeatureHandler<T>> {
        return try {
            val handler = getKoin().get<KnoxFeatureHandler<T>>(qualifier = named(feature.featureName))
            ApiResult.Success(handler)
        } catch(e: NoDefinitionFoundException) {
            ApiResult.Error(KnoxFeatureError.OperationFailed("No handler found for feature: ${feature.featureName}"))
        }
    }
}