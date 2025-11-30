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
        outputModuleName = "compose-previewl-lab"
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
            implementation(libs.kotestProperty)
            implementation(libs.kotlinxCoroutinesTest)
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
