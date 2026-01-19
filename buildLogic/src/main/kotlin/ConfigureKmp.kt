import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree
import util.COMMON_OPT_INS
import util.ModuleNameUtils
import util.android
import util.androidComponents
import util.kotlinMultiplatform

/**
 * Configures common KMP settings shared between KmpConventionPlugin and CmpConventionPlugin.
 *
 * @param moduleBaseNameProvider Provider for the module base name (called in afterEvaluate)
 */
@OptIn(ExperimentalWasmDsl::class, ExperimentalKotlinGradlePluginApi::class)
internal fun Project.configureKmp(
    moduleBaseNameProvider: () -> String,
) {
    android {
        compileSdk = 36

        defaultConfig {
            minSdk = 23
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    androidComponents {
        finalizeDsl {
            it.namespace = ModuleNameUtils.toAndroidNamespace(moduleBaseNameProvider())
        }
    }

    kotlinMultiplatform {
        jvmToolchain(17)

        androidTarget {
            instrumentedTestVariant.sourceSetTree.set(KotlinSourceSetTree.test)
        }

        jvm()

        js {
            browser()
            binaries.library()
            generateTypeScriptDefinitions()
            compilerOptions {
                this.target.set("es2015")
            }
        }

        wasmJs {
            browser()
            binaries.library()
            generateTypeScriptDefinitions()
            compilerOptions {
                this.target.set("es2015")
            }
        }

        listOf(
            iosX64(),
            iosArm64(),
            iosSimulatorArm64(),
        )

        applyDefaultHierarchyTemplate()

        compilerOptions {
            optIn.addAll(COMMON_OPT_INS)
        }

        sourceSets.commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }

    afterEvaluate {
        val moduleBaseName = moduleBaseNameProvider()
        val jsOutputModuleName = ModuleNameUtils.toJsOutputModuleName(moduleBaseName)
        val iosFrameworkBaseName = ModuleNameUtils.toIosFrameworkBaseName(moduleBaseName)

        kotlinMultiplatform {
            js {
                outputModuleName.set(jsOutputModuleName)
            }

            wasmJs {
                outputModuleName.set(jsOutputModuleName)
            }

            listOf(
                iosX64(),
                iosArm64(),
                iosSimulatorArm64(),
            ).forEach {
                it.binaries.framework {
                    baseName = iosFrameworkBaseName
                    isStatic = true
                }
            }
        }
    }
}
