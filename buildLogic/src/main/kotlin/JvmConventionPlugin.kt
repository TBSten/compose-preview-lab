import org.gradle.api.Plugin
import org.gradle.api.Project
import util.COMMON_OPT_INS
import util.alias
import util.kotlinJvm
import util.libs
import util.plugin
import util.plugins

class JvmConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        plugins {
            alias(libs.plugin("kotlinJvm"))
        }

        kotlinJvm {
            compilerOptions {
                optIn.addAll(COMMON_OPT_INS)
            }
        }
    }
}
