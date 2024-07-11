package com.example.starterapplication.core.knox.feature.domain.model

import com.example.starterapplication.core.knox.feature.presentation.KnoxFeatureState

data class KnoxFeature<out T> (
    val key: KnoxFeatureKey<T>,
    val state: KnoxFeatureState<T>
)