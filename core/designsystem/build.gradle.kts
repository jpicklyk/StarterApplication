plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.convention.android.library.compose)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.starterapplication.core.designsystem"
}

dependencies {
    api(libs.androidx.compose.material.iconsExtended)
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.runtime)
}