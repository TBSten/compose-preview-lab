package me.tbsten.compose.preview.lab

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

/**
 * A Gradle plugin for configuring the Compose Preview Lab.
 *
 * Registers the Kotlin Compiler Plugin that collects @Preview functions
 * and generates PreviewList/PreviewAllList at compile time.
 */
class ComposePreviewLabGradlePlugin : Plugin<Project>, KotlinCompilerPluginSupportPlugin {
    override fun apply(project: Project): Unit = with(project) {
        val extension = extensions.create("composePreviewLab", ComposePreviewLabExtension::class.java)
        configureFeaturedFiles(extension = extension)
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true

    override fun getCompilerPluginId(): String = "me.tbsten.compose.preview.lab.compiler"

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = "me.tbsten.compose.preview.lab",
        artifactId = "compiler-plugin",
        version = VERSION,
    )

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project
        val extension = project.extensions.findByType(ComposePreviewLabExtension::class.java)
            ?: return project.provider { emptyList() }

        return project.provider {
            listOf(
                SubpluginOption("previewsListPackage", extension.generatePackage),
                SubpluginOption("publicPreviewList", extension.publicPreviewList.toString()),
                SubpluginOption("projectRootPath", extension.projectRootPath),
                SubpluginOption("generatePreviewList", extension.generatePreviewList.toString()),
                SubpluginOption("generatePreviewAllList", extension.generatePreviewAllList.toString()),
            )
        }
    }

    companion object {
        // This should match the version published in the version catalog
        private val VERSION: String by lazy {
            ComposePreviewLabGradlePlugin::class.java
                .getResourceAsStream("/compose-preview-lab-version.txt")
                ?.bufferedReader()?.readLine()
                ?: throw IllegalStateException(
                    "Could not find compose-preview-lab-version.txt resource. " +
                        "Ensure the Gradle plugin is built correctly.",
                )
        }
    }
}
