plugins {
    alias(libs.plugins.convention.android.library)
    //alias(libs.plugins.convention.android.library.jacoco)
    alias(libs.plugins.convention.android.hilt)
}

android {
    namespace = "com.example.starterapplication.core.common"
}


dependencies {
    testImplementation(libs.kotlinx.coroutines.test)
}