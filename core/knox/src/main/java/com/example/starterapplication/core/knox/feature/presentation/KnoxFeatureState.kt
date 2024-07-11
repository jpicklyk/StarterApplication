package com.example.starterapplication.core.knox.feature.presentation

import com.example.starterapplication.core.knox.api.ApiResult

data class KnoxFeatureState<out T>(val enabled: Boolean, val value: T)

fun <T : Any> ApiResult<T>.transformToFeatureState(
    isEnabled: (T) -> Boolean
): ApiResult<KnoxFeatureState<T>> {
    return when (this) {
        is ApiResult.Success -> ApiResult.Success(KnoxFeatureState(isEnabled(data), data))
        is ApiResult.Error -> this
        is ApiResult.NotSupported -> this
    }
}