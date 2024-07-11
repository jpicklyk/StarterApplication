plugins {
    alias(libs.plugins.convention.android.application)
    alias(libs.plugins.convention.android.application.compose)
    alias(libs.plugins.convention.android.application.flavors)
    alias(libs.plugins.convention.android.koin)
    //To use the firebase plugin, you will need to update the google-services.json file to include
    //your project's specific firebase configuration.
    //alias(libs.plugins.convention.android.application.firebase)
    //id("com.google.android.gms.oss-licenses-plugin")
    alias(libs.plugins.baselineprofile)
}

android {
    namespace = "com.example.starterapplication"

    defaultConfig {
        applicationId = "com.example.starterapplication"
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        //TODO: Update for signing keys
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    packaging {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
        }
    }
    @Suppress("UnstableApiUsage")
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.knox)
    implementation(projects.knoxStandard)

    implementation(libs.koin.androidx.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.adaptive)
    implementation(libs.androidx.compose.material3.adaptive.layout)
    implementation(libs.androidx.compose.material3.adaptive.navigation)
    implementation(libs.androidx.compose.material3.adaptive.navigationSuite)
    implementation(libs.androidx.compose.material3.windowSizeClass)
    implementation(libs.androidx.ui.tooling)
    //implementation(libs.androidx.compose.runtime.tracing)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.test.ext)
    //implementation(libs.androidx.profileinstaller)
    //implementation(libs.androidx.tracing.ktx)
    //implementation(libs.androidx.window.core)
    //implementation(libs.kotlinx.coroutines.guava)
    //implementation(libs.coil.kt)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(platform(libs.koin.annotations.bom))
    implementation(libs.koin.annotations)
    ksp(libs.koin.ksp.compiler)
    //debugImplementation(libs.androidx.compose.ui.testManifest)
    //debugImplementation(projects.uiTestHiltManifest)


//    testImplementation(projects.core.dataTest)
    //testImplementation(projects.core.testing)
//    testImplementation(projects.sync.syncTest)
    testImplementation(libs.androidx.compose.ui.test)
    //testImplementation(libs.androidx.work.testing)
    testImplementation(libs.androidx.test.ext)


    //testDemoImplementation(libs.robolectric)
    //testDemoImplementation(libs.roborazzi)
    //testDemoImplementation(projects.core.screenshotTesting)

    androidTestImplementation(projects.core.testing)
//    androidTestImplementation(projects.core.dataTest)
//    androidTestImplementation(projects.core.datastoreTest)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.navigation.testing)
    androidTestImplementation(libs.androidx.compose.ui.test)
}