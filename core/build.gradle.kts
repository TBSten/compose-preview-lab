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
    jvmToolchain(17)
    androidTarget {
        // https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-test.html
        instrumentedTestVariant.sourceSetTree.set(KotlinSourceSetTree.test)
    }

    jvm()

    js {
        outputModuleName = "compose-previewl-lab-core"
        browser()
        binaries.executable()
        binaries.library()
        generateTypeScriptDefinitions()

        compilerOptions {
            target = "es2015"
        }
    }

    wasmJs {
        outputModuleName = "compose-previewl-lab"
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
            baseName = "ComposePreviewLabCore"
            isStatic = true
        }
    }

    applyDefaultHierarchyTemplate()
    sourceSets {
        commonMain.dependencies {
            api(projects.annotation)
            implementation("org.jetbrains.compose.runtime:runtime:1.11.0-alpha01")
            implementation("org.jetbrains.compose.foundation:foundation:1.11.0-alpha01")
            implementation("org.jetbrains.compose.components:components-resources:1.11.0-alpha01")
            implementation("org.jetbrains.compose.ui:ui:1.11.0-alpha01")
            implementation("org.jetbrains.compose.ui:ui-tooling-preview:1.11.0-alpha01")
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        androidMain.dependencies {
            implementation("org.jetbrains.compose.ui:ui-tooling:1.11.0-alpha01")
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
        }

        val otherJsMain by creating {
            dependsOn(commonMain.get())
        }
        listOf(
            androidMain,
            iosMain,
            jvmMain,
            wasmJsMain,
        ).forEach {
            it.get().dependsOn(otherJsMain)
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
