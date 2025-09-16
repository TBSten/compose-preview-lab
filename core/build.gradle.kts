@file:OptIn(ExperimentalKotlinGradlePluginApi::class, ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.conventionFormat)
    alias(libs.plugins.conventionPublish)
}

kotlin {
    jvmToolchain(11)
    androidTarget {
        // https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-test.html
        instrumentedTestVariant.sourceSetTree.set(KotlinSourceSetTree.test)
    }

    jvm()

    js {
        browser()
        binaries.executable()
    }

    wasmJs {
        browser()
        binaries.executable()
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach {
        it.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.components.resources)
            implementation(compose.ui)
            implementation(compose.components.uiToolingPreview)
            implementation("org.jetbrains.compose.material:material-ripple:${libs.versions.compose.get()}")
            implementation(libs.dokar3Sonner)
            // TODO migrate retain { } (compose runtime api)
            implementation("io.github.takahirom.rin:rin:0.3.0")
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        androidMain.dependencies {
            implementation(compose.uiTooling)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
        jsMain.dependencies {
            implementation(compose.html.core)
        }
    }
}

android {
    namespace = "me.tbsten.compose.preview.lab"
    compileSdk = 36

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

// https://developer.android.com/develop/ui/compose/testing#setup
dependencies {
    androidTestImplementation(libs.androidx.uitest.junit4)
    debugImplementation(libs.androidx.uitest.testManifest)
}

// for library development configuration

kotlin {
    applyDefaultHierarchyTemplate()

    compilerOptions {
        optIn.addAll(
            "me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi",
            "me.tbsten.compose.preview.lab.InternalComposePreviewLabApi",
        )
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

publishConvention {
    artifactName = "Core"
    artifactId = "core"
    description =
        "A component catalog library that collects and lists @Preview. \n" +
        "By providing APIs such as Field, Event, etc., it provides not only display but also interactive preview.\n" +
        "core provides runtime APIs such as annotations and gallery UI needed to collect Previews."
}
