package com.example.starterapplication.knox_standard.policy.usb.domain.usecase

import android.content.Context
import com.example.starterapplication.core.knox.api.ApiResult
import com.example.starterapplication.core.knox.usecase.CoroutineApiUseCase
import com.samsung.android.knox.EnterpriseDeviceManager
import javax.inject.Inject


class IsUsbHostStorageAllowedUseCase @Inject constructor(
    context: Context
) : CoroutineApiUseCase<Unit, Boolean>() {
    private val restrictionPolicy = EnterpriseDeviceManager.getInstance(context).restrictionPolicy

    override suspend fun execute(params: Unit): ApiResult<Boolean> {
        return ApiResult.Success(restrictionPolicy.isUsbHostStorageAllowed)
    }
}