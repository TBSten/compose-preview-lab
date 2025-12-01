import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.dokka.gradle.DokkaExtension
import util.dokka
import util.libs
import util.plugin

fun Project.configureDokka(publishConventionExtension: PublishConventionExtension) {
    pluginManager.apply(libs.plugin("dokka").pluginId)

    afterEvaluate {
        extensions.configure<DokkaExtension> {
            moduleName.set(publishConventionExtension.artifactName ?: project.name)
        }

        dokka {
            moduleName.set(publishConventionExtension.dokkaModuleName)
        }
    }
}
