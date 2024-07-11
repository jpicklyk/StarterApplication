plugins {
    alias(libs.plugins.convention.android.feature)
    alias(libs.plugins.convention.android.knox.license)
}

android {
    namespace = "com.example.starterapplication.knox_standard"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        testInstrumentationRunner = "com.example.starterapplication.knox_standard.KoinTestRunner"
    }

    packaging {
        resources {
            excludes += listOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "/META-INF/LICENSE*",
                "/META-INF/NOTICE*"
            )
        }
    }


}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.knox)
    //implementation(libs.core.ktx)
    implementation(libs.androidx.core.ktx)

    testImplementation(libs.junit)
    testImplementation(libs.bundles.mockk)
    //testImplementation(libs.androidx.runtime.android)
    testCompileOnly(fileTree("libs/knoxsdk.jar"))

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.androidx.test.uiautomator)
    androidTestCompileOnly(fileTree("libs/knoxsdk.jar"))

    androidTestUtil(libs.androidx.test.orchestrator)
    androidTestUtil(libs.androidx.test.services)

    // The Knox SDK shall not be available outside this module
    compileOnly(fileTree("libs/knoxsdk.jar"))
}

