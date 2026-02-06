plugins {
    alias(libs.plugins.convention.android.feature)
    alias(libs.plugins.convention.android.library.compose)
}

android {
    namespace = "com.example.starterapplication.feature.policies"
}

dependencies {
    // Compose
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.iconsExtended)

    // Knox framework types (PolicyRegistry, PolicyComponent, ApiResult, etc.)
    implementation(projects.knoxCore.feature)
    implementation(projects.knoxCore.usecaseExecutor)

    // License status checking (KnoxLicenseInitializer, LicenseStartupResult)
    implementation(projects.knoxLicensing)

    // Testing
    testImplementation(projects.knoxCore.testing)
    testImplementation(libs.junit)
    testImplementation(libs.bundles.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.hilt.android.testing)
}
