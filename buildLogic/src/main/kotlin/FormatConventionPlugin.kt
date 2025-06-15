import org.gradle.api.Plugin
import org.gradle.api.Project
import util.alias
import util.ktlint
import util.libs
import util.plugin
import util.plugins
import util.version

class FormatConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        plugins {
            alias(libs.plugin("ktlintGradle"))
        }

        ktlint {
            version.set(libs.version("ktlint"))
            filter {
                exclude { element ->
                    element.file.path.contains("generated/") || element.file.path.contains("resources/")
                }
                exclude("kotlin/me/tbsten/compose/preview/lab/theme/AppTheme.kt")
            }
        }
    }
}
