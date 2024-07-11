package com.example.starterapplication.core.knox.feature.domain.handler

import com.example.starterapplication.core.knox.feature.domain.model.KnoxFeatureKey
import org.koin.core.qualifier.named

inline fun <reified T : KnoxFeatureKey<*>> featureHandlerQualifier() =
    named("FeatureHandler_${T::class.simpleName}")