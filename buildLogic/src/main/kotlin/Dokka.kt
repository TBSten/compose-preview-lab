import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.dokka.gradle.DokkaTask
import util.libs
import util.plugin

internal fun Project.configureDokka(publishConventionExtension: PublishConventionExtension) {
    pluginManager.apply(libs.plugin("dokka").pluginId)

    afterEvaluate {
        tasks.withType<DokkaTask>().configureEach {
            moduleName.set(publishConventionExtension.artifactName ?: project.name)
        }
    }
}
