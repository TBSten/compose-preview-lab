package me.tbsten.compose.preview.lab

import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
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
            val depFqns = collectDependencyCollectPreviewsFqns(project)

            buildList {
                add(SubpluginOption("previewsListPackage", extension.generatePackage))
                add(SubpluginOption("projectRootPath", extension.projectRootPath))
                if (depFqns.isNotEmpty()) {
                    add(SubpluginOption("dependencyCollectPreviewsFqns", depFqns.joinToString(",")))
                }
            }
        }
    }

    /**
     * 依存プロジェクトの `composePreviewLab { collectPreviewsExport }` を収集する。
     *
     * `collectAllModulePreviews()` のクロスモジュール集約に使用する。
     */
    private fun collectDependencyCollectPreviewsFqns(project: Project): List<String> = project.configurations
        .flatMap { it.allDependencies }
        .filterIsInstance<ProjectDependency>()
        .mapNotNull { dep ->
            // Kotlin Gradle Plugin 2.4+ では ProjectDependency.dependencyProject が削除されたため、
            // path から rootProject.findProject で解決する
            val depProject = project.rootProject.findProject(dep.path) ?: return@mapNotNull null
            depProject.extensions.findByType(ComposePreviewLabExtension::class.java)
                ?.collectPreviewsExport
                ?.takeIf { it.isNotBlank() }
        }
        .distinct()
}
