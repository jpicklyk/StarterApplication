// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()

    }
    dependencies {
        /*
        classpath(libs.google.oss.licenses.plugin) {
            exclude(group = "com.google.protobuf")
        }
         */
    }

}

// Lists all plugins used throughout the project
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.baselineprofile) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    //alias(libs.plugins.dependencyGuard) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.firebase.perf) apply false
    alias(libs.plugins.gms) apply false
    alias(libs.plugins.ksp) apply false
    //alias(libs.plugins.roborazzi) apply false
    //alias(libs.plugins.secrets) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.kotlin.android) apply false
    //alias(libs.plugins.module.graph) apply true // Plugin applied to allow module graph generation
}
