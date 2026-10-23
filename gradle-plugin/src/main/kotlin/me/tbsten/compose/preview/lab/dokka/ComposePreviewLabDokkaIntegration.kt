package me.tbsten.compose.preview.lab.dokka

import org.gradle.api.Project
import org.jetbrains.dokka.gradle.DokkaExtension

/**
 * Wires [ComposePreviewLabDokkaPluginParameters] into the Dokka Gradle plugin (v2).
 *
 * Kept in its own class so that `org.jetbrains.dokka.gradle.*` is only loaded once the Dokka Gradle plugin
 * has actually been applied to the project (see `ComposePreviewLabGradlePlugin`).
 */
internal object ComposePreviewLabDokkaIntegration {
    const val DokkaGradlePluginId: String = "org.jetbrains.dokka"

    /**
     * Registers the `composePreviewLab` entry in `dokka.pluginsConfiguration`.
     *
     * ```kotlin
     * // What a consumer can then write – no import required:
     * dokka {
     *     pluginsConfiguration {
     *         composePreviewLab { baseUrl = "https://example.com/compose-preview-lab-gallery" }
     *     }
     * }
     * ```
     */
    fun register(project: Project) {
        val dokka = project.extensions.findByType(DokkaExtension::class.java)
        if (dokka == null) {
            // Dokka Gradle plugin running in V1 compatibility mode has no `DokkaExtension`.
            project.logger.info(
                "Compose Preview Lab: Dokka Gradle plugin v2 extension not found in ${project.path}; " +
                    "skipping `pluginsConfiguration.composePreviewLab` registration.",
            )
            return
        }
        dokka.pluginsConfiguration.registerBinding(
            ComposePreviewLabDokkaPluginParameters::class.java,
            ComposePreviewLabDokkaPluginParameters::class.java,
        )
        dokka.pluginsConfiguration.register(
            ComposePreviewLabDokkaPluginParameters.Name,
            ComposePreviewLabDokkaPluginParameters::class.java,
        )
    }
}
