# Knox Starter Application

A Samsung Knox Android application demonstrating enterprise policy management with modern Android architecture. Built with Jetpack Compose, Hilt, and a DI-agnostic multi-module design that separates Knox policy logic from dependency injection concerns.

## Highlights

- **124+ enterprise policies** across 16 domain categories (connectivity, security, telephony, etc.)
- **KSP code generation** — annotate a policy class with `@PolicyDefinition` and the processor generates components, registry keys, and type-safe lookups
- **DI-agnostic core** — Knox modules work standalone; the `knox-hilt` module adds Hilt bindings for apps that use it
- **ApiResult\<T\>** sealed class used everywhere — no thrown exceptions from use cases
- **Convention plugins** — 14 Gradle plugins in `build-logic/` keep module configuration DRY

## Architecture

The project follows a layered multi-module architecture inspired by [Now in Android](https://developer.android.com/series/now-in-android):

```
┌─────────────────────────────────────────────┐
│                    app                       │
│         (MainActivity, Navigation)           │
├──────────┬──────────┬───────────────────────┤
│ feature/ │ feature/ │ feature/              │
│   home   │ license  │ policies              │
├──────────┴──────────┴───────────────────────┤
│              knox-hilt                       │
│        (Hilt DI bindings)                    │
├─────────────────┬───────────────────────────┤
│  knox-enterprise│  knox-licensing            │
│  (SDK policies) │  (license mgmt)            │
├─────────────────┴───────────────────────────┤
│               knox-core/*                    │
│  feature │ android │ common │ ui │ usecase   │
├─────────────────────────────────────────────┤
│            core/designsystem                 │
│         (Material3 theme tokens)             │
└─────────────────────────────────────────────┘
```

Knox core and enterprise modules have **no Hilt dependency**. The `knox-hilt` module bridges them into the DI graph for apps that use Hilt. KSP-generated code uses a service locator (`AndroidApplicationContextProvider`) since KSP runs before Hilt's annotation processing.

## Module Structure

### Application & Features

| Module | Description |
|--------|-------------|
| `app` | Entry point: `MainActivity`, `RootApplication`, navigation host |
| `feature:home` | Home screen with license status summary and navigation cards |
| `feature:license` | License management screen, device admin receiver |
| `feature:policies` | Policy list with search/filter, ViewModel, and tests |

### Knox Core (DI-agnostic framework)

| Module | Description |
|--------|-------------|
| `knox-core:feature` | Policy framework: `@PolicyDefinition`, registry, grouping strategies, capabilities |
| `knox-core:feature-processor` | KSP processor — generates `*Component`, `*Key`, and registry entries |
| `knox-core:android` | `AndroidApplicationContextProvider` service locator for KSP-generated code |
| `knox-core:common` | DataStore, preferences, coroutine utilities |
| `knox-core:ui` | Shared Compose components for policy UI |
| `knox-core:usecase-executor` | Base use case classes, `ApiResult<T>` sealed type |
| `knox-core:testing` | Fakes, test rules (`AndroidContextProviderRule`, device requirements) |

See [knox-core/README.md](knox-core/README.md) for framework details.

### Knox Implementation

| Module | Description |
|--------|-------------|
| `knox-enterprise` | 124+ Knox SDK policy implementations across 16 domains |
| `knox-licensing` | License activation & management with Flow-based state observation |
| `knox-hilt` | Hilt DI bindings bridging DI-agnostic Knox modules |

See per-module READMEs: [knox-enterprise](knox-enterprise/README.md) · [knox-licensing](knox-licensing/README.md) · [knox-hilt](knox-hilt/README.md)

### Build System

| Module | Description |
|--------|-------------|
| `core:designsystem` | Material3 theme, color tokens, typography |
| `build-logic/convention` | 14 Gradle convention plugins for consistent module configuration |

See [build-logic/README.md](build-logic/README.md) for convention plugin details.

## Tech Stack

| Technology | Version | Notes |
|------------|---------|-------|
| Kotlin | 2.1.10 | K2 compiler, JVM 11 target |
| Jetpack Compose | BOM 2025.04.00 | Material3 |
| Hilt | 2.54 | With KSP (not KAPT) |
| KSP | 2.1.10-1.0.29 | Code generation for policies |
| AndroidX Navigation | 2.8.9 | Compose navigation |
| Coroutines | 1.8.0 | Structured concurrency throughout |
| Knox SDK | v38 | `compileOnly` — consumers provide the JAR |
| Min SDK | 31 | Android 12 |
| Target SDK | 34 | Android 14 |
| Compile SDK | 35 | Android 15 |

## Getting Started

### Prerequisites

- **Android Studio** (latest stable recommended)
- **Samsung Knox SDK JAR** (`knoxsdk_ver38.jar`) — place in `knox-enterprise/libs/` and `knox-licensing/libs/`
- **JDK 17+** for Gradle (JVM 11 target for compiled code)

### Build

```bash
# Clone the repository
git clone <repository-url>
cd StarterApplication

# Build the project
./gradlew build 2>&1

# Run unit tests
./gradlew test 2>&1

# Build a specific module
./gradlew :app:build 2>&1
./gradlew :knox-enterprise:build 2>&1
./gradlew :feature:policies:build 2>&1

# Run a single test class
./gradlew :feature:policies:test --tests "com.example.starterapplication.feature.policies.PolicyListViewModelTest" 2>&1

# Lint checks
./gradlew check 2>&1

# Clean build
./gradlew clean build 2>&1
```

### Customize Package Name

Rename scripts update the package name, app display name, and project name across the entire codebase:

- **Windows**: `.\UpdatePackageName.ps1`
- **Mac/Linux**: `./update_package_name.sh`

### Knox Licensing Setup

To use Knox policies on a device, you need a Knox Platform for Enterprise (KPE) license:

1. **Create a Samsung Knox Developer account** at [developer.samsungknox.com](https://developer.samsungknox.com/)

2. **Obtain a KPE license key** — follow the [Knox Platform for Enterprise license guide](https://docs.samsungknox.com/admin/knox-platform-for-enterprise/before-you-begin/knox-platform-for-enterprise-licenses/) to generate your license key

3. **Add the license to `local.properties`** in the project root:

   ```properties
   knox.license=KLM09-XXXX...XXX
   ```

4. **Set a unique package name** — run the rename script to update the package name across the codebase:

   - **Windows**: `.\UpdatePackageName.ps1`
   - **Mac/Linux**: `./update_package_name.sh`

5. **Bind the application to your license** — the compiled application must be bound to your license key in the Knox console for the activation process to succeed. Register your app's package name and signing certificate in your Knox developer portal.

## Key Concepts

### Policy Definition & Code Generation

Annotate a policy class with `@PolicyDefinition` and the KSP processor generates boilerplate:

```kotlin
@PolicyDefinition(
    title = "Allow Airplane Mode",
    description = "Controls whether airplane mode can be toggled",
    category = PolicyCategory.Toggle,
    capabilities = [PolicyCapability.RADIO, PolicyCapability.AFFECTS_CONNECTIVITY]
)
class AllowAirplaneModePolicy : BooleanStatePolicy(stateMapping = StateMapping.INVERTED) {
    // ...
}
```

**Generated artifacts per policy:**
- `*Component` — wraps the policy, implements `PolicyComponent<T>`
- `*Key` — type-safe registry lookup key
- Registry entry in `GeneratedPolicyComponents`
- `PolicyType` sealed interface member for exhaustive matching

### ApiResult\<T\>

All Knox operations return `ApiResult<T>` instead of throwing exceptions:

```kotlin
sealed class ApiResult<out T : Any> {
    data class Success<out T : Any>(val data: T) : ApiResult<T>()
    data class Error(val apiError: ApiError, val exception: Exception?) : ApiResult<Nothing>()
    data object NotSupported : ApiResult<Nothing>()
}
```

### Policy Domains

The `knox-enterprise` module organizes 124+ policies into 16 domain categories:

| Domain | Examples |
|--------|----------|
| connectivity | WiFi, Bluetooth, NFC, airplane mode, tethering, roaming |
| security | CC mode, encryption, clipboard, USB debugging, biometrics |
| telephony | SMS/MMS/call limits, emergency call, caller ID, SIM copy |
| device | Factory reset, backup, developer mode, power settings |
| password | STIG compliance: length, complexity, lock delay, biometric |
| media | Camera, audio/video recording, microphone, screen capture |
| browser | Popups, JavaScript, fraud warning, cookies, autofill |
| hardware | SD card, home key, headphone, USB media player |
| application | App allowlist, AI cloud apps, payment apps, voice assistant |
| firewall | Domain filter, firewall rules, domain filter reports |
| display | Toast messages, LCD backlight, display mirroring |
| statusbar | Notifications, icons, clock, screen off on double tap |
| system | USB mass storage, power menu, infrared, charger sound |
| datetime | Date/time change, automatic time |
| audio | Volume panel |

## Testing

The project uses **MockK** (not Mockito) for mocking and **Google Truth** for assertions. Fakes are preferred over mocks where available.

```bash
# All tests
./gradlew test 2>&1

# Module tests
./gradlew :knox-core:feature:test 2>&1
./gradlew :feature:policies:test 2>&1
```

ViewModel tests use `StandardTestDispatcher` with `Dispatchers.setMain()`. For `WhileSubscribed` flows, tests use `backgroundScope.launch { flow.collect {} }` to keep the flow active during assertions.

## License

**Knox Starter Application** is distributed under the terms of the Apache License (Version 2.0). See the [LICENSE](LICENSE) file for details.
