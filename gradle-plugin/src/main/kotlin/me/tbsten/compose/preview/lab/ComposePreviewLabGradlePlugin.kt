package me.tbsten.compose.preview.lab

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * A Gradle plugin for configuring the Compose Preview Lab.
 */
public class ComposePreviewLabGradlePlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = with(project) {
        val extension = extensions.create("composePreviewLab", ComposePreviewLabExtension::class.java)
        applyToKspExtension(extension = extension)
        configureFeaturedFiles(extension = extension)
    }
}
