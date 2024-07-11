package com.example.starterapplication.knox_standard.policy.usb.domain.handler

import com.example.starterapplication.core.knox.api.ApiResult
import com.example.starterapplication.core.knox.feature.domain.handler.FeatureHandler
import com.example.starterapplication.core.knox.feature.domain.handler.KnoxFeatureHandler
import com.example.starterapplication.core.knox.feature.presentation.KnoxFeatureState
import com.example.starterapplication.core.knox.feature.presentation.transformToFeatureState
import com.example.starterapplication.knox_standard.policy.usb.domain.usecase.AllowUsbHostStorageUseCase
import com.example.starterapplication.knox_standard.policy.usb.domain.usecase.IsUsbHostStorageAllowedUseCase


@FeatureHandler
class UsbHostStorageHandler (
    private val getUseCase: IsUsbHostStorageAllowedUseCase,
    private val setUseCase: AllowUsbHostStorageUseCase
) : KnoxFeatureHandler<Boolean> {
    override suspend fun getState(): ApiResult<KnoxFeatureState<Boolean>> =
        // Since the APIResult will be true/false, we don't need complex calculation.
        // Just return the data which is of type boolean back
        getUseCase().transformToFeatureState { it }
    override suspend fun setState(newState: KnoxFeatureState<Boolean>): ApiResult<Unit> =
        setUseCase(newState.enabled)
}