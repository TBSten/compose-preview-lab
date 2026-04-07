import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    // NOTE: "me.tbsten.compose.preview.lab" must be applied BEFORE composeCompiler
    id("me.tbsten.compose.preview.lab")
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
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
    }

    @Suppress("OPT_IN_USAGE")
    wasmJs {
        browser()

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
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
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
        }
    }
}

android {
    namespace = "me.tbsten.compose.preview.lab.sample.uiLib"
    compileSdk = 36
}

composePreviewLab {
    publicPreviewList = true
    collectPreviewsExport = "uiLib.uiLibPreviews"
}
