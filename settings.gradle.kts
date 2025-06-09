// settings.gradle.kts

import org.gradle.api.initialization.resolve.RepositoriesMode

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
    plugins {
        // Android Gradle Plugin
        id("com.android.application") version "8.10.1"
        // Kotlin Android plugin
        id("org.jetbrains.kotlin.android") version "1.9.0"
        // Google Services plugin
        id("com.google.gms.google-services") version "4.3.15"
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "Water My Flower"
include(":app")
