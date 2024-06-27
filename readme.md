Starter Application Project
===========================

This starter application project is meant to help get new projects off the ground quickly while focusing on modern Android architecture practices.
The application heavily utilizes concepts from the [Now in Android](https://developer.android.com/series/now-in-android)
app. You can use the `UpdatePackageName.ps1` PowerShell script to replace the package name structure with your own.
Make note that this project template is a **work in progress**

# Architecture

The starter project follows the
[official architecture guidance](https://developer.android.com/topic/architecture)
and is described in detail in the
[architecture learning journey](docs/ArchitectureLearningJourney.md).

# Modularization

The starter project has been fully modularized and you can find the detailed guidance and
description of the modularization strategy used in
[modularization learning journey](docs/ModularizationLearningJourney.md).

# Build

The app contains the usual `debug` and `release` build variants.

In addition, the `benchmark` variant of `app` is used to test startup performance and generate a
baseline profile (see below for more information).


# License

**Starter Application** is distributed under the terms of the Apache License (Version 2.0). See the
[license](LICENSE) for more information.