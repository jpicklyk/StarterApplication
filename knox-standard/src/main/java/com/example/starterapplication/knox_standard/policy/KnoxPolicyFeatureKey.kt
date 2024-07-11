package com.example.starterapplication.knox_standard.policy

import com.example.starterapplication.core.knox.feature.domain.model.KnoxFeatureKey

sealed class KnoxPolicyFeatureKey<T>(override val featureName: String) : KnoxFeatureKey<T> {
    data object UsbHostStorage : KnoxPolicyFeatureKey<Boolean>("USB_HOST_STORAGE")
    // Add other feature keys as needed
}