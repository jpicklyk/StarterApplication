package com.example.starterapplication.knox_standard.di

import com.example.starterapplication.core.knox.feature.domain.handler.KnoxFeatureHandler
import com.example.starterapplication.core.knox.feature.domain.registry.KnoxFeatureCategory
import com.example.starterapplication.core.knox.feature.domain.registry.KnoxFeatureRegistry
import com.example.starterapplication.knox_standard.license.di.ComponentScanModule
import com.example.starterapplication.knox_standard.policy.KnoxPolicyFeatureKey
import com.example.starterapplication.knox_standard.policy.usb.domain.handler.UsbHostStorageHandler
import com.example.starterapplication.knox_standard.policy.usb.domain.usecase.AllowUsbHostStorageUseCase
import com.example.starterapplication.knox_standard.policy.usb.domain.usecase.IsUsbHostStorageAllowedUseCase
import org.koin.core.qualifier.named

import org.koin.dsl.module
import org.koin.ksp.generated.module

fun knoxStandardModule() = module {
    includes(
        ComponentScanModule().module,
    )

    // Register KnoxFeatureRegistry and features
    single {
        KnoxFeatureRegistry().apply {
            registerFeature(KnoxPolicyFeatureKey.UsbHostStorage, KnoxFeatureCategory.PRODUCTION)
        }
    }

    // Provide use cases
    factory { IsUsbHostStorageAllowedUseCase(get()) }
    factory { AllowUsbHostStorageUseCase(get()) }

    // Register handlers
    single<KnoxFeatureHandler<Boolean>>(named(KnoxPolicyFeatureKey.UsbHostStorage.featureName)) {
        UsbHostStorageHandler(get(), get())
    }

}