import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinxSerialization)
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

    js {
        browser()
        binaries.executable()
        binaries.library()
        generateTypeScriptDefinitions()

        compilerOptions {
            target = "es2015"
        }
    }

    @Suppress("OPT_IN_USAGE")
    wasmJs {
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
            baseName = "UiLib"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.debugmenu)

            implementation(libs.composeRuntime)
            implementation(libs.composeFoundation)
            implementation(libs.composeMaterial3)
            implementation(libs.composeUiToolingPreview)
            implementation("me.tbsten.compose.preview.lab:starter:${libs.versions.composePreviewLab.get()}")

            implementation("me.tbsten.compose.preview.lab:extension-kotlinx-datetime:${libs.versions.composePreviewLab.get()}")
            implementation(libs.kotlinxDatetime)

            implementation("me.tbsten.compose.preview.lab:extension-navigation:${libs.versions.composePreviewLab.get()}")
            implementation(libs.androidxNavigation)
            implementation(libs.kotlinxSerializationCore)

            implementation("me.tbsten.compose.preview.lab:extension-debugger:${libs.versions.composePreviewLab.get()}")
        }
    }
}

android {
    namespace = "me.tbsten.compose.preview.lab.sample.uiLib"
    compileSdk = 36

    defaultConfig {
        minSdk = 23

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    val composePreviewLabKspPlugin =
        "me.tbsten.compose.preview.lab:ksp-plugin:${libs.versions.composePreviewLab.get()}"
    add("kspCommonMainMetadata", composePreviewLabKspPlugin)
    add("kspJvm", composePreviewLabKspPlugin)
    add("kspJs", composePreviewLabKspPlugin)
    add("kspAndroid", composePreviewLabKspPlugin)
}

composePreviewLab {
    publicPreviewList = true
}
