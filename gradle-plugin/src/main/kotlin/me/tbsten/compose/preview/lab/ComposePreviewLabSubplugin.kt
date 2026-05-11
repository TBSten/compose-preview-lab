package me.tbsten.compose.preview.lab

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

class ComposePreviewLabSubplugin : KotlinCompilerPluginSupportPlugin {

    override fun apply(target: Project) {
    }

    override fun getCompilerPluginId(): String = PreviewLabCompilerPluginId

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = PluginGroupId,
        artifactId = PluginArtifactId,
        version = PluginVersion,
    )

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project

        if (project.supportsCompilerPluginOrder()) {
            kotlinCompilation.compileTaskProvider.configure {
                compilerOptions.freeCompilerArgs.add(
                    "-Xcompiler-plugin-order=$PreviewLabCompilerPluginId>$ComposeCompilerPluginId",
                )
            }
        }

        val extension = project.extensions.getByType<ComposePreviewLabExtension>()

        return project.provider {
            listOf(
                SubpluginOption("projectRootPath", extension.projectRootPath.get()),
                SubpluginOption("collectPreviewsEnabled", extension.collectPreviews.enabled.get().toString()),
                SubpluginOption("defaultCollectScope", extension.collectPreviews.defaultCollectScope.get()),
            )
        }
    }
}
