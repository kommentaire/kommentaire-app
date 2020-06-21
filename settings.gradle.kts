rootProject.name="kommentaire-app"

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        jcenter()
        google()
        mavenLocal {
            content {
                includeVersionByRegex("com.apollographql.apollo", ".*", ".*-SNAPSHOT")
            }
        }
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.android.application") {
                useModule("com.android.tools.build:gradle:${requested.version}")
            }
        }
    }
}

include(":android-app", "lib")