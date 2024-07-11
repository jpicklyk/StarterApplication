# Knox Feature Management System

This document provides an overview and documentation for the Knox Feature Management System. It includes explanations of key components, usage examples, and best practices.

## Overview

The Knox Feature Management System provides a flexible and type-safe way to handle feature flags and dynamic configurations in our Android application. It allows for easy toggling of features, management of feature states, and categorization of features (e.g., production, deprecated). A Knox feature could represent a single policy or potentially a larger group of functionality.

## Key Components

- `KnoxFeatureKey`: Interface for defining feature keys.
- `KnoxFeatureState`: Represents the state of a feature (enabled/disabled and associated value).
- `KnoxFeatureHandler`: Interface for handling feature-specific operations.
- `KnoxFeatureManager`: Central manager for interacting with features.
- `KnoxFeatureRegistry`: Keeps track of all registered features and their categories.
- `KnoxFeatureHandlerFactory`: Factory for creating feature handlers.

## Usage

### 1. Defining a New Feature

a. Create a new sealed class that implements `KnoxFeatureKey`:

```kotlin
sealed class MyFeatureKey<T>(override val featureName: String) : KnoxFeatureKey<T> {
    data object NEW_FEATURE : MyFeatureKey<Boolean>("new_feature")
}
```

b. Implement a `KnoxFeatureHandler` for your feature:

```kotlin
@FeatureHandler
class MyFeatureHandler(
    private val getUseCase: GetMyFeatureUseCase,
    private val setUseCase: SetMyFeatureUseCase
) : KnoxFeatureHandler<Boolean> {
    override suspend fun getState(): ApiResult<KnoxFeatureState<Boolean>> =
        getUseCase().mapToFeatureState()
    override suspend fun setState(newState: KnoxFeatureState<Boolean>): ApiResult<Unit> =
        setUseCase(newState.value)
}
```

c. Register your feature in your Koin module:

```kotlin
fun myFeatureModule() = module {
    single<KnoxFeatureHandler<Boolean>>(named(MyFeatureKey.NEW_FEATURE.featureName)) {
        MyFeatureHandler(get(), get())
    }

    single {
        val registry: KnoxFeatureRegistry = get()
        registry.registerFeature(MyFeatureKey.NEW_FEATURE, KnoxFeatureCategory.PRODUCTION)
        registry
    }
}
```

### 2. Using Features in Your Code

Inject the `KnoxFeatureManager` into your ViewModel or use case:

```kotlin
class MyViewModel(
    private val featureManager: KnoxFeatureManager
) : ViewModel() {
    suspend fun checkNewFeature() {
        when (val result = featureManager.getFeatureState(MyFeatureKey.NEW_FEATURE)) {
            is ApiResult.Success -> {
                if (result.data.enabled) {
                    // Feature is enabled, use the new functionality
                } else {
                    // Feature is disabled, use the old functionality
                }
            }
            is ApiResult.Error -> {
                // Handle error
            }
            is ApiResult.NotSupported -> {
                // Handle not supported case
            }
        }
    }

    suspend fun enableNewFeature() {
        featureManager.setFeatureState(MyFeatureKey.NEW_FEATURE, KnoxFeatureState(true, true))
    }
}
```

### 3. Getting All Features

You can retrieve all features or features by category:

```kotlin
suspend fun getAllFeatures() {
    when (val result = featureManager.getAllFeatures()) {
        is ApiResult.Success -> {
            // Process all features
        }
        is ApiResult.Error -> {
            // Handle error
        }
        is ApiResult.NotSupported -> {
            // Handle not supported case
        }
    }
}

suspend fun getDeprecatedFeatures() {
    when (val result = featureManager.getAllFeatures(KnoxFeatureCategory.DEPRECATED)) {
        is ApiResult.Success -> {
            // Process deprecated features
        }
        is ApiResult.Error -> {
            // Handle error
        }
        is ApiResult.NotSupported -> {
            // Handle not supported case
        }
    }
}
```

## Best Practices

1. Always use the `KnoxFeatureManager` to interact with features, rather than accessing feature states directly.
2. Keep feature keys in a centralized place (like a sealed class) for easy management.
3. Use meaningful names for your feature keys and handlers.
4. Write unit tests for your feature handlers and use cases.
5. Use the `@FeatureHandler` annotation for all classes implementing `KnoxFeatureHandler`.
6. Utilize Koin's `named()` qualifier when registering feature handlers to ensure proper injection.
7. Make use of the `mapToFeatureState()` extension function when wrapping API results in `KnoxFeatureState`.

For more detailed information about specific components, refer to their respective class and interface definitions in the codebase.