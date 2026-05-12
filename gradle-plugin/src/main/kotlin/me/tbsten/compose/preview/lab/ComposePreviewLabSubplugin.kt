package me.tbsten.compose.preview.lab

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

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
            // task-034: `kotlin-dsl` plugin を外したため receiver の型が KotlinCompilationTask<*> へ
            // 推論されない場合に備え、明示的にパラメータ型を指定する。
            kotlinCompilation.compileTaskProvider.configure { task: KotlinCompilationTask<*> ->
                task.compilerOptions.freeCompilerArgs.add(
                    "-Xcompiler-plugin-order=$PreviewLabCompilerPluginId>$ComposeCompilerPluginId",
                )
            }
        }

        val extension = project.extensions.getByType<ComposePreviewLabExtension>()

        return project.provider {
            listOf(
                SubpluginOption("projectRootPath", extension.projectRootPath),
                SubpluginOption("collectPreviewsEnabled", extension.collectPreviews.enabled.toString()),
                SubpluginOption("defaultCollectScope", extension.collectPreviews.defaultCollectScope),
            )
        }
    }
}
