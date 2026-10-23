import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.kotlin.dsl.configure
import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.dokka.gradle.engine.plugins.DokkaPluginParametersBaseSpec
import org.jetbrains.dokka.gradle.internal.InternalDokkaGradlePluginApi
import util.dokka
import util.libs
import util.plugin

private const val DOKKA_PLUGIN_PROJECT_PATH = ":dokka-plugin"

/** Gallery of this repository's own previews, deployed by `preview-web.yml` next to the docs site. */
private const val COMPOSE_PREVIEW_LAB_GALLERY_URL =
    "https://tbsten.github.io/compose-preview-lab/compose-preview-lab-gallery"

/**
 * `pluginsConfiguration` entry for this repository's own `:dokka-plugin`.
 *
 * Library modules do not apply the published `me.tbsten.compose.preview.lab` Gradle plugin (which would
 * register `ComposePreviewLabDokkaPluginParameters` for us), so the convention plugin carries a minimal
 * spec of its own. The JSON mirrors `ComposePreviewLabDokkaConfiguration` in `:dokka-plugin`.
 */
@OptIn(InternalDokkaGradlePluginApi::class)
abstract class ComposePreviewLabDokkaParameters @Inject constructor(name: String) :
    DokkaPluginParametersBaseSpec(name, "me.tbsten.compose.preview.lab.dokka.ComposePreviewLabDokkaPlugin") {
    @get:Input
    abstract val baseUrl: Property<String>

    override fun jsonEncode(): String = """{"baseUrl":"${baseUrl.get()}"}"""
}

fun Project.configureDokka(publishConventionExtension: PublishConventionExtension) {
    pluginManager.apply(libs.plugin("dokka").pluginId)

    if (path != DOKKA_PLUGIN_PROJECT_PATH) {
        // Every library module renders its own KDoc with the `@previewLab` tag support, pointing at the
        // hosted gallery so an embedded preview resolves on the published site.
        dependencies.add("dokkaPlugin", dependencies.project(mapOf("path" to DOKKA_PLUGIN_PROJECT_PATH)))
        dokka {
            pluginsConfiguration.registerBinding(
                ComposePreviewLabDokkaParameters::class.java,
                ComposePreviewLabDokkaParameters::class.java,
            )
            pluginsConfiguration.register("composePreviewLab", ComposePreviewLabDokkaParameters::class.java) {
                baseUrl.set(COMPOSE_PREVIEW_LAB_GALLERY_URL)
            }
        }
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
