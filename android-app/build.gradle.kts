plugins {
    id("com.android.application")
    kotlin("android")
}

android {

    compileSdkVersion(30)
    defaultConfig {
        applicationId = "fr.kommentaire.app"
        minSdkVersion(21)
        targetSdkVersion(30)
        versionCode = 2
        versionName = "0.0.2"
    }

    signingConfigs {
        create("mbonnin") {
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
            storeFile = project.file("keystore.jks")
            storePassword = System.getenv("STORE_PASSWORD")
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("mbonnin")
        }
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.3.0-alpha01")
    implementation("androidx.ui:ui-material:0.1.0-dev13")
    implementation("androidx.ui:ui-tooling:0.1.0-dev13")
    implementation("androidx.ui:ui-material-icons-extended:0.1.0-dev13")
    implementation(project(":lib"))
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.2.0")
    //implementation("com.apollographql.apollo:apollo-runtime:2.2.2-SNAPSHOT")
    //implementation("com.apollographql.apollo:apollo-coroutines-support:2.2.2-SNAPSHOT")
}
