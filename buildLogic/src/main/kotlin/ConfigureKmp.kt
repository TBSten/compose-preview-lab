import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree
import util.COMMON_OPT_INS
import util.ModuleNameUtils
import util.android
import util.androidComponents
import util.kotlinMultiplatform
import util.libs
import util.version

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
        compileSdk = libs.version("androidCompileSdk").toInt()

        defaultConfig {
            minSdk = libs.version("androidMinSdk").toInt()
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    androidComponents {
        finalizeDsl {
            it.namespace = ModuleNameUtils.toAndroidNamespace(moduleBaseNameProvider())
        }
    }

    val jsTarget = libs.version("jsTarget")

    kotlinMultiplatform {
        jvmToolchain(libs.version("jvmToolchain").toInt())

        androidTarget {
            instrumentedTestVariant.sourceSetTree.set(KotlinSourceSetTree.test)
        }

        jvm()

        js {
            browser()
            binaries.library()
            generateTypeScriptDefinitions()
            compilerOptions {
                this.target.set(jsTarget)
            }
        }

        wasmJs {
            browser()
            binaries.library()
            generateTypeScriptDefinitions()
            compilerOptions {
                this.target.set(jsTarget)
            }
        }

        iosX64()
        iosArm64()
        iosSimulatorArm64()

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

            targets.withType(org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget::class.java)
                .matching { it.konanTarget.family == org.jetbrains.kotlin.konan.target.Family.IOS }
                .configureEach {
                    binaries.framework {
                        baseName = iosFrameworkBaseName
                        isStatic = true
                    }
                }
        }
    }
}
