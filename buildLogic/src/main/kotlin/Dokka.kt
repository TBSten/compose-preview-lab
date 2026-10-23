import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.dokka.gradle.DokkaExtension
import util.dokka
import util.libs
import util.plugin

private const val DOKKA_PLUGIN_PROJECT_PATH = ":dokka-plugin"

fun Project.configureDokka(publishConventionExtension: PublishConventionExtension) {
    pluginManager.apply(libs.plugin("dokka").pluginId)

    if (path != DOKKA_PLUGIN_PROJECT_PATH) {
        dependencies.add("dokkaPlugin", dependencies.project(mapOf("path" to DOKKA_PLUGIN_PROJECT_PATH)))
    }

    afterEvaluate {
        extensions.configure<DokkaExtension> {
            moduleName.set(publishConventionExtension.artifactName ?: project.name)
        }

        dokka {
            moduleName.set(publishConventionExtension.dokkaModuleName)
        }
    }
}
