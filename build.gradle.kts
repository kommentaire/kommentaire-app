/*buildscript {
    repositories {
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.2.0")
    }
}*/

plugins {
    kotlin("multiplatform").version("1.3.72").apply(false)
    id("com.android.application").version("4.2.0-alpha02").apply(false)
    id("com.apollographql.apollo").version("2.2.2-SNAPSHOT").apply(false)
}

subprojects {
    repositories {
        google()
        mavenCentral()
        jcenter()
        mavenLocal {
            content {
                includeVersionByRegex("com.apollographql.apollo", ".*", ".*-SNAPSHOT")
            }
        }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    plugins.withType(com.android.build.gradle.BasePlugin::class.java) {
        configure<com.android.build.gradle.BaseExtension> {
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_1_8
                targetCompatibility = JavaVersion.VERSION_1_8
            }
        }
    }
}

