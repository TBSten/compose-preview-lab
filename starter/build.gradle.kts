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
        outputModuleName = "compose-preview-lab-starter"
        browser()
        binaries.executable()
        binaries.library()
        generateTypeScriptDefinitions()

        compilerOptions {
            target = "es2015"
        }
    }

    wasmJs {
        outputModuleName = "compose-preview-lab-starter"
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
            baseName = "ComposePreviewLabStarter"
            isStatic = true
        }
    }

    applyDefaultHierarchyTemplate()
    sourceSets {
        commonMain.dependencies {
            api(projects.core)
            api(projects.field)
            api(projects.ui)
            api(projects.previewLab)
            api(projects.gallery)
        }
    }
}

android {
    namespace = "me.tbsten.compose.preview.lab.starter"
    compileSdk = 36

    defaultConfig {
        minSdk = 21
    }
}

publishConvention {
    artifactName = "Starter"
    artifactId = "starter"
    description =
        "Compose Preview Lab starter module. " +
        "This module bundles all core modules (core, field, ui, preview-lab, gallery) " +
        "for easy setup. Just add this single dependency to get started with Compose Preview Lab."
}
