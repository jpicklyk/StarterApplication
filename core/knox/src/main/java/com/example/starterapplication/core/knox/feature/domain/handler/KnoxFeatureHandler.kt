package com.example.starterapplication.core.knox.feature.domain.handler

import com.example.starterapplication.core.knox.api.ApiResult
import com.example.starterapplication.core.knox.feature.presentation.KnoxFeatureState

interface KnoxFeatureHandler<T> {
    suspend fun getState(): ApiResult<KnoxFeatureState<T>>
    suspend fun setState(newState: KnoxFeatureState<T>): ApiResult<Unit>
}