import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.compose.ComposePlugin
import util.alias
import util.kotlinMultiplatform
import util.library
import util.libs
import util.plugin
import util.plugins

/**
 * Configures common CMP (Compose Multiplatform) settings shared between
 * CmpConventionPlugin and CmpUiConventionPlugin.
 *
 * This includes:
 * - Applying kotlinMultiplatform, androidLibrary, composeCompiler, composeMultiplatform plugins
 * - Calling configureKmp() for KMP settings
 * - Adding composeRuntime to commonMain
 * - Adding composeUiTooling to androidMain
 * - Adding compose.desktop.currentOs to jvmMain
 * - Adding androidTest dependencies
 *
 * @param moduleBaseNameProvider Provider for the module base name (called in afterEvaluate)
 */
internal fun Project.configureCmp(
    moduleBaseNameProvider: () -> String,
) {
    plugins {
        alias(libs.plugin("kotlinMultiplatform"))
        alias(libs.plugin("androidLibrary"))
        alias(libs.plugin("composeCompiler"))
        alias(libs.plugin("composeMultiplatform"))
    }

    configureKmp(moduleBaseNameProvider)

    kotlinMultiplatform {
        sourceSets.commonMain.dependencies {
            implementation(libs.library("composeRuntime"))
        }
        sourceSets.androidMain.dependencies {
            implementation(libs.library("composeUiTooling"))
        }
        sourceSets.jvmMain.dependencies {
            implementation(ComposePlugin.Dependencies(project).desktop.currentOs)
        }
    }

    dependencies {
        add("androidTestImplementation", libs.library("androidxUitestJunit4"))
        add("debugImplementation", libs.library("androidxUitestTestManifest"))
    }
}
