import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.reload.gradle.ComposeHotRun
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeFeatureFlag

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose)
    // FIXME enable android application debug
//    alias(libs.plugins.android.application)
    alias(libs.plugins.hotReload)
    alias(libs.plugins.ksp)
}

kotlin {
    jvmToolchain(11)
//    androidTarget {
//        // https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-test.html
//        @Suppress("OPT_IN_USAGE")
//        instrumentedTestVariant.sourceSetTree.set(KotlinSourceSetTree.test)
//    }

    jvm()

    js {
        browser()
        binaries.executable()
    }

    @Suppress("OPT_IN_USAGE")
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
            implementation(projects.core)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            // TODO migrate retain { } (compose runtime api)
            implementation("io.github.takahirom.rin:rin:0.3.0")
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

//        androidMain.dependencies {
//            implementation(compose.uiTooling)
//            implementation(libs.androidx.activityCompose)
//        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
        }

        jsMain.dependencies {
            implementation(compose.html.core)
        }
    }
}

// android {
//    namespace = "me.tbsten.compose.preview.lab"
//    compileSdk = 36
//
//    defaultConfig {
//        minSdk = 21
//        targetSdk = 36
//
//        applicationId = "me.tbsten.compose.preview.lab.androidApp"
//        versionCode = 1
//        versionName = "1.0.0"
//
//        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
//    }
// }

// // https://developer.android.com/develop/ui/compose/testing#setup
// dependencies {
//    androidTestImplementation(libs.androidx.uitest.junit4)
//    debugImplementation(libs.androidx.uitest.testManifest)
// }

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Compose Preview Lab"
            packageVersion = "1.0.0"

            linux {
                iconFile.set(project.file("desktopAppIcons/LinuxIcon.png"))
            }
            windows {
                iconFile.set(project.file("desktopAppIcons/WindowsIcon.ico"))
            }
            macOS {
                iconFile.set(project.file("desktopAppIcons/MacosIcon.icns"))
                bundleID = "me.tbsten.compose.preview.lab.sample.desktopApp"
            }
        }
    }
}

// https://github.com/JetBrains/compose-hot-reload
composeCompiler {
    featureFlags.add(ComposeFeatureFlag.OptimizeNonSkippingGroups)
}
tasks.register<ComposeHotRun>("runHot") {
    mainClass.set("MainKt")
}
