package com.example.starterapplication.knox_standard.policy.usb.domain.usecase

import android.content.Context
import com.example.starterapplication.core.knox.api.ApiResult
import com.example.starterapplication.core.knox.api.internals.DefaultApiError
import com.example.starterapplication.core.knox.usecase.CoroutineApiUseCase
import com.samsung.android.knox.EnterpriseDeviceManager

class AllowUsbHostStorageUseCase (
    context: Context
) : CoroutineApiUseCase<Boolean, Unit>() {
    private val restrictionPolicy = EnterpriseDeviceManager.getInstance(context).restrictionPolicy
    override suspend fun execute(params: Boolean?): ApiResult<Unit> {
        requireNotNull(params) { "params cannot be null" }
        val result = restrictionPolicy.allowUsbHostStorage(params)
        return if (result)
            ApiResult.Success(Unit)
        else
            ApiResult.Error(DefaultApiError.UnexpectedError())
    }
}