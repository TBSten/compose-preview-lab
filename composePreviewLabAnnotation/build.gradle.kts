import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.conventionFormat)
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
    }

    wasmJs {
        browser()
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "ComposePreviewLabAnnotation"
            isStatic = true
        }
    }

    sourceSets {
    }
}

android {
    namespace = "me.tbsten.compose.preview.lab.annotation"
    compileSdk = 35

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}
