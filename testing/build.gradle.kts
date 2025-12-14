@file:OptIn(ExperimentalKotlinGradlePluginApi::class, ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.library)
    alias(libs.plugins.conventionFormat)
    alias(libs.plugins.conventionPublish)
}

kotlin {
    jvmToolchain(17)
    androidTarget {
        instrumentedTestVariant.sourceSetTree.set(KotlinSourceSetTree.test)
    }

    jvm()

    js {
        outputModuleName = "compose-preview-lab-testing"
        browser()
        binaries.executable()
        binaries.library()
        generateTypeScriptDefinitions()

        compilerOptions {
            target = "es2015"
        }
    }

    wasmJs {
        outputModuleName = "compose-preview-lab-testing"
        browser()
        binaries.executable()
        binaries.library()
        generateTypeScriptDefinitions()

        compilerOptions {
            target = "es2015"
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach {
        it.binaries.framework {
            baseName = "ComposePreviewLabTesting"
            isStatic = true
        }
    }

    applyDefaultHierarchyTemplate()
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core)
            implementation(projects.previewLab)
            implementation(projects.annotation)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(libs.androidxLifecycleViewmodel)
            implementation(libs.androidxLifecycleRuntimeCompose)
            api(libs.kotestProperty)
        }
    }
}

android {
    namespace = "me.tbsten.compose.preview.lab.testing"
    compileSdk = 36

    defaultConfig {
        minSdk = 21
    }
}

kotlin {
    applyDefaultHierarchyTemplate()

    compilerOptions {
        optIn.addAll(
            "me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi",
            "me.tbsten.compose.preview.lab.InternalComposePreviewLabApi",
        )
    }
}

publishConvention {
    artifactName = "Testing"
    artifactId = "testing"
    description =
        "Testing utilities for Compose Preview Lab. " +
        "Provides TestPreviewLab composable and field access helpers for UI testing."
}
