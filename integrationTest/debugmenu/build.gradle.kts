plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.androidLibrary)
}

kotlin {
    jvmToolchain(17)
    androidTarget()
    jvm()
    js {
        browser()
    }

    @Suppress("OPT_IN_USAGE")
    wasmJs {
        browser()
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach {
        it.binaries.framework {
            baseName = "DebugMenu"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.composeRuntime)
            implementation(libs.composeFoundation)
            implementation(libs.composeMaterial3)

            implementation("me.tbsten.compose.preview.lab:starter:${libs.versions.composePreviewLab.get()}")
            implementation("me.tbsten.compose.preview.lab:extension-debugger:${libs.versions.composePreviewLab.get()}")
        }
    }
}

android {
    namespace = "me.tbsten.compose.preview.lab.sample.debugmenu"
    compileSdk = 36
    defaultConfig {
        minSdk = 23
    }
}
