plugins {
    id("com.android.library")
    kotlin("multiplatform")
    id("com.apollographql.apollo")
}

kotlin {
    android()
    ios {
        binaries {
            framework {
                //freeCompilerArgs = freeCompilerArgs + "-Xobjc-generics"
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.7")
                api("com.apollographql.apollo:apollo-api:2.2.3-SNAPSHOT")
                api("com.apollographql.apollo:apollo-runtime-kotlin:2.2.3-SNAPSHOT")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val androidMain by getting {
            dependencies {
                implementation("androidx.startup:startup-runtime:1.0.0-alpha01")
                implementation("com.facebook.flipper:flipper:0.47.0")
                implementation("com.facebook.soloader:soloader:0.9.0")
                implementation("com.facebook.flipper:flipper-network-plugin:0.47.0")
                implementation("com.squareup.okhttp3:logging-interceptor:4.7.2")
            }
        }
    }
}

android {

    compileSdkVersion(30)
    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(30)
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}