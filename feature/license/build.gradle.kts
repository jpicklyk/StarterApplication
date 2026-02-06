plugins {
    alias(libs.plugins.convention.android.feature)
    alias(libs.plugins.convention.android.library.compose)
    alias(libs.plugins.convention.android.knox.license)
}

android {
    namespace = "com.example.starterapplication.feature.license"
}

dependencies {
    // Compose
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.iconsExtended)
    implementation(libs.androidx.activity.compose)

    // Knox licensing
    implementation(projects.knoxLicensing)
}
