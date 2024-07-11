Starter Application Project
===========================

This starter application project is meant to help get new projects off the ground quickly while focusing on modern Android architecture practices.
The application heavily utilizes concepts from the [Now in Android](https://developer.android.com/series/now-in-android)
app. You can use the `UpdatePackageName.ps1` PowerShell script to replace the package name structure with your own.
Make note that this project template is a **work in progress**

# Architecture

The starter project closely follows the
[official architecture guidance](https://developer.android.com/topic/architecture)
and is described in detail in the
[architecture learning journey](https://developer.android.com/topic/architecture/docs/ArchitectureLearningJourney.md).

This particular sample replaces Dagger Hilt with Koin which is a native Kotlin DI framework.  More information about Koin can be found here:
[Koin](https://insert-koin.io/)

# Modularization

The starter project has been fully modularized and you can find the detailed guidance and
description of the modularization strategy used in
[modularization learning journey](https://github.com/android/nowinandroid/blob/main/docs/ModularizationLearningJourney.md).

# Build

The app contains the usual `debug` and `release` build variants.

In addition, the `benchmark` variant of `app` is used to test startup performance and generate a
baseline profile (see below for more information).

## Samsung Knox Setup

1. Create a `local.properties` file in the root of the project if it doesn't exist.
2. Add the following line to `local.properties`, replacing `your_actual_license_key_here` with the actual Knox license key:
   knox.license=your_actual_license_key_here

The 'KnoxLicenseConventionPlugin' will retrieve the key and add it to the BuildConfig using the 
key: KNOX_LICENSE_KEY.  Production applications should take care to protect the keys and keep them 
secure.  Load from a secure keystore or import from an encrypted file.

The Knox functionality is contained within its own module 'knox-standard' and imports the sdk using
'compileOnly' as to maintain a separation of concerns from the presentation of the application.
This application takes the approach of wrapping the Knox APIs into use cases and provides a base
coroutine class `CoroutineApiUseCase` and helper functions similar to DSL for execution/handling
`ApiUseCaseExecutor`.

# License

**Starter Application** is distributed under the terms of the Apache License (Version 2.0). See the
[license](LICENSE) for more information.