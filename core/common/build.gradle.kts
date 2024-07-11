plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.convention.android.koin)
}

android {
    namespace = "com.example.starterapplication.core.common"
}

dependencies {
    implementation(libs.koin.android)
    testImplementation(libs.kotlinx.coroutines.test)
}