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
        outputModuleName = "compose-previewl-lab-preview-lab"
        browser()
        binaries.executable()
        binaries.library()
        generateTypeScriptDefinitions()

        compilerOptions {
            target = "es2015"
        }
    }

    wasmJs {
        outputModuleName = "compose-previewl-lab-preview-lab"
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
            baseName = "ComposePreviewLabPreviewLab"
            isStatic = true
        }
    }

    applyDefaultHierarchyTemplate()
    sourceSets {
        val otherWeb by creating {
            dependsOn(commonMain.get())
        }
        listOf(androidMain, jvmMain, iosMain).forEach {
            it.get().dependsOn(otherWeb)
        }

        commonMain.dependencies {
            api(projects.core)
            api(projects.ui)
            api(projects.field)
            implementation("org.jetbrains.compose.runtime:runtime:1.11.0-alpha01")
            implementation("org.jetbrains.compose.foundation:foundation:1.11.0-alpha01")
            implementation("org.jetbrains.compose.components:components-resources:1.11.0-alpha01")
            implementation("org.jetbrains.compose.ui:ui:1.11.0-alpha01")
            implementation("org.jetbrains.compose.ui:ui-tooling-preview:1.11.0-alpha01")
            implementation(libs.kotlinxSerializationJson)
            implementation(libs.filekitCore)
            implementation(libs.filekitDialogs)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        jvmTest.dependencies {
            implementation(libs.kotestProperty)
            implementation(libs.kotlinxCoroutinesTest)
        }
        androidMain.dependencies {
            implementation("org.jetbrains.compose.ui:ui-tooling:1.11.0-alpha01")
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}

compose.resources {
    packageOfResClass = "me.tbsten.compose.preview.lab.previewlab.generated.resources"
}

android {
    namespace = "me.tbsten.compose.preview.lab.previewlab"
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
    artifactName = "PreviewLab"
    artifactId = "preview-lab"
    description =
        "A component catalog library that collects and lists @Preview. \n" +
        "By providing APIs such as Field, Event, etc., it provides not only display but also interactive preview.\n" +
        "preview-lab provides <TODO>"
}
