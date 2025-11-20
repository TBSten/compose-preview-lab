import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.dokka.gradle.DokkaExtension
import util.libs
import util.plugin

internal fun Project.configureDokka(publishConventionExtension: PublishConventionExtension) {
    pluginManager.apply(libs.plugin("dokka").pluginId)

    extensions.configure<DokkaExtension> {
        moduleName.set(publishConventionExtension.artifactName ?: project.name)
    }
}
