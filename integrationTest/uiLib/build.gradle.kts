import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    id("me.tbsten.compose.preview.lab")
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.dokka)
}

dependencies {
    // Compose Preview Lab dokka plugin: embeds @previewLab <previewId> tags as <iframe>s
    // pointing at the hosted Compose Preview Lab gallery.
    // Resolved via the composite build (`includeBuild("../")`) — no mavenLocal needed
    // when running from this repo. External projects should depend on the published
    // coordinate `me.tbsten.compose.preview.lab:dokka-plugin:<version>` and add
    // `mavenLocal()` (or Maven Central once published).
    dokkaPlugin("me.tbsten.compose.preview.lab:dokka-plugin:${libs.versions.composePreviewLab.get()}")
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

