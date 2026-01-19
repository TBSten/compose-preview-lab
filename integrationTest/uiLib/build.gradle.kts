@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.ksp)
    id("me.tbsten.compose.preview.lab")
}

kotlin {
    jvmToolchain(17)
    androidTarget {
        // https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-test.html
        @Suppress("OPT_IN_USAGE")
        instrumentedTestVariant.sourceSetTree.set(KotlinSourceSetTree.test)
    }

    jvm()

    js { browser() }

    wasmJs { browser() }

    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach {
        it.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.composeRuntime)
            implementation(libs.composeFoundation)
            implementation(libs.composeMaterial3)
            implementation(libs.composeComponentsResources)
            implementation(libs.composeUiToolingPreview)
            implementation("me.tbsten.compose.preview.lab:starter:${libs.versions.composePreviewLab.get()}")
        }

        commonMain.dependencies {
            implementation(libs.composeRuntime)
            implementation(libs.composeFoundation)
            implementation(libs.composeMaterial3)
            implementation(libs.composeComponentsResources)
            implementation(libs.composeUiToolingPreview)
            implementation("me.tbsten.compose.preview.lab:starter:${libs.versions.composePreviewLab.get()}")

            implementation("me.tbsten.compose.preview.lab:extension-kotlinx-datetime:${libs.versions.composePreviewLab.get()}")
            implementation(libs.kotlinxDatetime)

            implementation("me.tbsten.compose.preview.lab:extension-navigation:${libs.versions.composePreviewLab.get()}")
            implementation(libs.androidxNavigation)
            implementation(libs.kotlinxSerializationCore)
        }
    }
}

android {
    namespace = "me.tbsten.compose.preview.lab"
    compileSdk = 36

    defaultConfig {
        minSdk = 23

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

// https://developer.android.com/develop/ui/compose/testing#setup
dependencies {
    androidTestImplementation(libs.androidxUitestJunit4)
    debugImplementation(libs.androidxUitestTestManifest)

    val composePreviewLabKspPlugin =
        "me.tbsten.compose.preview.lab:ksp-plugin:${libs.versions.composePreviewLab.get()}"
    add("kspCommonMainMetadata", composePreviewLabKspPlugin)
    add("kspAndroid", composePreviewLabKspPlugin)
    add("kspJvm", composePreviewLabKspPlugin)
    add("kspJs", composePreviewLabKspPlugin)
    add("kspWasmJs", composePreviewLabKspPlugin)
}

composePreviewLab {
    publicPreviewList = true
}
