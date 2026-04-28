import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

// Reversed plugin order fixture: composeCompiler is applied BEFORE me.tbsten.compose.preview.lab.
// On Kotlin 2.3+, -Xcompiler-plugin-order injected by the gradle-plugin ensures correct IR ordering.
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeCompiler)
    id("me.tbsten.compose.preview.lab")
    alias(libs.plugins.composeMultiplatform)
}

kotlin {
    jvmToolchain(17)

    androidTarget {
        @Suppress("OPT_IN_USAGE")
        instrumentedTestVariant.sourceSetTree.set(KotlinSourceSetTree.test)
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.composeRuntime)
            implementation(libs.composeFoundation)
            implementation(libs.composeMaterial3)
            implementation(libs.composeUiToolingPreview)
            implementation("me.tbsten.compose.preview.lab:starter:${libs.versions.composePreviewLab.get()}")
        }
    }
}

android {
    namespace = "me.tbsten.compose.preview.lab.sample.uiLibReversed"
    compileSdk = 36
}
