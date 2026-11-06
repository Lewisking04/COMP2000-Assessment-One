plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.comp2000roombookingapp"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.comp2000roombookingapp"
        minSdk = 26   // java.time (dates and times in the demo data) needs API 26
        targetSdk = 34   // 34 keeps the toolbar from overlapping the content (Android 15 forces edge-to-edge for 35+)
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation(libs.recyclerview)   // used for the room and booking lists
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}