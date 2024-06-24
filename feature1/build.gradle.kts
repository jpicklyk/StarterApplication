plugins {
    alias(libs.plugins.convention.android.feature)
    alias(libs.plugins.convention.android.library.compose)
}

android {
    namespace = "com.example.starterapplication.feature.feature1"
}

dependencies {

    testImplementation(projects.core.testing)

    androidTestImplementation(projects.core.testing)
}