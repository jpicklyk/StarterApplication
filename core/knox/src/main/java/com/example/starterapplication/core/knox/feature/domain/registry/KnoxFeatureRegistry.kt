package com.example.starterapplication.core.knox.feature.domain.registry

import android.util.Log
import com.example.starterapplication.core.knox.feature.domain.model.KnoxFeatureKey
import org.koin.core.annotation.Single

enum class KnoxFeatureCategory {
    PRODUCTION,
    DEPRECATED
}

@Single
class KnoxFeatureRegistry {
    private val tag = "KnoxFeatureRegistry"
    private val features = mutableMapOf<KnoxFeatureCategory, MutableSet<KnoxFeatureKey<*>>>()

    fun registerFeature(feature: KnoxFeatureKey<*>, category: KnoxFeatureCategory = KnoxFeatureCategory.PRODUCTION) {
        Log.d(tag, "registerFeature: $feature with category: $category")
        features.getOrPut(category) { mutableSetOf() }.add(feature)
    }

    fun getFeatures(category: KnoxFeatureCategory? = null): Set<KnoxFeatureKey<*>> {
        Log.d(tag, "getFeatures: category: $category")
        return category?.let { features[it] } ?: features.values.flatten().toSet()
    }

    fun getCategorizedFeatures(): Map<KnoxFeatureCategory, Set<KnoxFeatureKey<*>>> {
        Log.d(tag, "getCategorizedFeatures")
        return features.mapValues { it.value.toSet() }
    }
}