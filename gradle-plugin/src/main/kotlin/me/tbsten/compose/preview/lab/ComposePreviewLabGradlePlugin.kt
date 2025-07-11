package me.tbsten.compose.preview.lab

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

/**
 * A Gradle plugin for configuring the Compose Preview Lab.
 */
class ComposePreviewLabGradlePlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = with(project) {
        plugins.apply(PublicPreviewPlugin::class)

        val extension = extensions.create("composePreviewLab", ComposePreviewLabExtension::class.java)
        applyToKspExtension(extension = extension)
        configureFeaturedFiles(extension = extension)
    }
}
