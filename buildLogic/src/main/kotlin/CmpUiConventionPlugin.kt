import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import util.alias
import util.kotlinMultiplatform
import util.library
import util.libs
import util.plugin
import util.plugins

class CmpUiConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        val extension = extensions.create<CmpConventionExtension>("cmpConvention")
        configureCmp(moduleBaseNameProvider = { extension.moduleBaseName.get() })

        plugins {
            alias(libs.plugin("hotReload"))
        }

        kotlinMultiplatform {
            sourceSets.commonMain.dependencies {
                implementation(libs.library("composeFoundation"))
                implementation(libs.library("composeComponentsResources"))
                implementation(libs.library("composeUi"))
                implementation(libs.library("composeUiToolingPreview"))
            }
        }
    }
}
