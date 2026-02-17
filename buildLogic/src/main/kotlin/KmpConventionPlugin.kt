import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import util.alias
import util.libs
import util.plugin
import util.plugins

class KmpConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        val extension = extensions.create<KmpConventionExtension>("kmpConvention")

        plugins {
            alias(libs.plugin("kotlinMultiplatform"))
            alias(libs.plugin("androidLibrary"))
        }

        configureKmp(
            moduleBaseNameProvider = { extension.moduleBaseName.get() },
        )
    }
}
