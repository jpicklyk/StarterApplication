# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with this project.

## Project Overview

StarterApplication is a Samsung Knox Android app demonstrating enterprise policy management. It uses a multi-module architecture with DI-agnostic Knox libraries and a Hilt integration layer.

## Build Commands

**IMPORTANT**: Never pipe Gradle output through `tail`, `head`, or other stream processors (e.g., `./gradlew build 2>&1 | tail -30`). This causes pipe buffer hangs on Windows/MSYS with long-running builds. Always run Gradle commands directly and let the Bash tool handle output truncation.

```bash
# Full build
./gradlew build 2>&1

# Build specific modules
./gradlew :app:build 2>&1
./gradlew :feature:policies:build 2>&1
./gradlew :knox-enterprise:build 2>&1
./gradlew :knox-hilt:build 2>&1

# Run all unit tests
./gradlew test 2>&1

# Run specific module tests
./gradlew :app:test 2>&1
./gradlew :feature:policies:test 2>&1
./gradlew :knox-core:feature:test 2>&1

# Run single test class
./gradlew :feature:policies:test --tests "com.example.starterapplication.feature.policies.PolicyListViewModelTest" 2>&1

# Clean build
./gradlew clean build 2>&1

# Lint check
./gradlew check 2>&1

# Stop Gradle daemon
./gradlew --stop
```

## Module Structure

```
app                    → Entry point: MainActivity, navigation, feature screens
core/designsystem      → Material3 theme, color tokens, typography
core/common            → Shared utilities
core/testing           → Test infrastructure
knox-core/feature      → Policy framework, annotations, registry, grouping
knox-core/feature-processor → KSP processor for @PolicyDefinition
knox-core/android      → AndroidApplicationContextProvider (service locator)
knox-core/common       → DataStore, preferences, coroutine utilities
knox-core/ui           → Shared Compose components for policy UI
knox-core/usecase-executor → Base use case classes, ApiResult<T>
knox-core/testing      → Fakes, test rules
knox-enterprise        → Knox SDK policy implementations by domain
knox-licensing         → License activation & management
knox-hilt              → Hilt DI bindings (bridges DI-agnostic modules)
feature/home           → Home screen with license status summary and navigation
feature/license        → License management screen, device admin receiver
feature/policies       → Policy list screen, ViewModel, and tests
```

See per-module `CLAUDE.md` files for detailed module guidance:
- [knox-core/CLAUDE.md](knox-core/CLAUDE.md)
- [knox-enterprise/CLAUDE.md](knox-enterprise/CLAUDE.md)
- [knox-hilt/CLAUDE.md](knox-hilt/CLAUDE.md)
- [knox-licensing/CLAUDE.md](knox-licensing/CLAUDE.md)

## Key Architectural Rules

1. **DI-Agnostic Knox Modules**: `knox-core` and `knox-enterprise` must never depend on Hilt. All Hilt bindings go in `knox-hilt`.
2. **KSP before Hilt**: Generated code uses service locator (`WithAndroidApplicationContext`) since KSP runs before Hilt's annotation processing.
3. **ApiResult<T> everywhere**: All Knox operations return `ApiResult<T>` (Success/Error/NotSupported). Never throw exceptions from use cases.
4. **Convention plugins**: Module build files use plugins from `build-logic/convention/`. Don't add raw dependencies that a convention plugin already provides.
5. **compileOnly Knox SDK**: `knox-enterprise` and `knox-licensing` use `compileOnly` for the Knox JAR. The app provides it at runtime.

## Packages

| Scope | Package |
|-------|---------|
| App | `com.example.starterapplication` |
| Knox core | `net.sfelabs.knox.core.*` |
| Knox enterprise | `net.sfelabs.knox_enterprise.*` |
| Knox licensing | `com.github.jpicklyk.knox.licensing.*` |
| Knox hilt | `net.sfelabs.knox.hilt.*` |

## Naming Conventions

- Screens: `[Feature]Screen.kt` (e.g., `PolicyListScreen.kt`)
- ViewModels: `[Feature]ViewModel.kt` (e.g., `PolicyListViewModel.kt`)
- Policies: `[Feature]Policy.kt` (e.g., `AllowAirplaneModePolicy.kt`)
- Use cases: `[Action][Entity]UseCase.kt` (e.g., `SetAdbStateUseCase.kt`)
- Hilt modules: `[Domain]Module.kt`, installed in `SingletonComponent`
- Use case parameter: always name it `params` in `execute()` methods

## UI Patterns

- Compose screens accept `viewModel: VM = hiltViewModel()` as a parameter
- State exposed as `StateFlow<UiState>` collected with `collectAsState()`
- Derived state uses `.map().stateIn(viewModelScope, WhileSubscribed(5000), initial)`
- Navigation via sealed class `Screen` with route strings in `NavHost`

## Testing

- **MockK** (NOT Mockito) for mocking
- **Truth** for assertions
- **Fakes over mocks** when available (`FakePreferencesRepository`, `FakeDataStoreSource`)
- ViewModel tests: `StandardTestDispatcher` + `Dispatchers.setMain()`
- WhileSubscribed flows: use `backgroundScope.launch { flow.collect {} }` to activate
- Knox instrumented tests: `AndroidContextProviderRule` for context setup

## Build System

- **Version catalog**: `gradle/libs.versions.toml`
- **Convention plugins**: `build-logic/convention/` (Kotlin DSL)
- **Key plugins**: `convention.android.feature`, `convention.android.hilt`, `convention.android.library.compose`
- **Kotlin**: 2.1.10 (K2 compiler), JVM 11 target with desugaring
- **Compose BOM**: 2025.04.00
- **Gradle**: parallel=true, caching=true, configuration-cache=true

## Git Workflow

- **Active branch**: `main-knox-hilt`
- **PR target**: `master`
