plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.convention.android.koin)
}

android {
    namespace = "com.example.starterapplication.core.knox"
}

dependencies {
    implementation(libs.koin.core.coroutines)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.bundles.mockk)
    testImplementation(libs.koin.test)
    testImplementation(libs.koin.test.junit)
}