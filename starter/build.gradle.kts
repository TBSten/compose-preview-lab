@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
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
        outputModuleName = "compose-previewl-lab-starter"
        browser()
    }

    wasmJs {
        outputModuleName = "compose-previewl-lab-starter"
        browser()
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    applyDefaultHierarchyTemplate()
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
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
        minSdk = 23
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
