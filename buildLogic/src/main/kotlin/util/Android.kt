package util

import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.gradle.LibraryExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Common opt-in annotations used across all convention plugins.
 */
val COMMON_OPT_INS = listOf(
    "me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi",
    "me.tbsten.compose.preview.lab.InternalComposePreviewLabApi",
)

/**
 * Configures the Android library extension.
 * Usage: `android { compileSdk = 36 }`
 */
fun Project.android(block: LibraryExtension.() -> Unit) {
    extensions.configure<LibraryExtension>(block)
}

/**
 * Configures the Android library components extension.
 * Usage: `androidComponents { finalizeDsl { ... } }`
 */
fun Project.androidComponents(block: LibraryAndroidComponentsExtension.() -> Unit) {
    extensions.configure<LibraryAndroidComponentsExtension>(block)
}

/**
 * Configures the Kotlin JVM extension.
 * Usage: `kotlinJvm { compilerOptions { ... } }`
 */
fun Project.kotlinJvm(block: KotlinJvmProjectExtension.() -> Unit) {
    extensions.configure<KotlinJvmProjectExtension>(block)
}

/**
 * Configures the Kotlin Multiplatform extension.
 * Usage: `kotlinMultiplatform { jvmToolchain(17) }`
 */
fun Project.kotlinMultiplatform(block: KotlinMultiplatformExtension.() -> Unit) {
    extensions.configure<KotlinMultiplatformExtension>(block)
}
