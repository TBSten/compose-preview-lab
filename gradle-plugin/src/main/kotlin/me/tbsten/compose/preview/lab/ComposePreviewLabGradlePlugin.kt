package me.tbsten.compose.preview.lab

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * A Gradle plugin for configuring the Compose Preview Lab.
 */
class ComposePreviewLabGradlePlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = with(project) {
        extensions.create("composePreviewLab", ComposePreviewLabExtension::class.java)
        pluginManager.apply(ComposePreviewLabSubplugin::class.java)
        val extension = extensions.getByType(ComposePreviewLabExtension::class.java)
        configureFeaturedFiles(extension = extension)
        if (!supportsCompilerPluginOrder()) {
            validatePluginOrder()
        }
    }

    /**
     * Compose Compiler Plugin より前に適用されていることを検証する。
     *
     * Compose Preview Lab の IR extension が @Composable ラムダを生成し、
     * Compose Compiler がそれを ComposableLambda に変換する必要があるため、
     * 適用順序が逆だとランタイムエラーになる。
     */
    private fun Project.validatePluginOrder() {
        val composeCompilerPluginId = "org.jetbrains.kotlin.plugin.compose"
        if (pluginManager.hasPlugin(composeCompilerPluginId)) {
            throw GradleException(
                "Compose Preview Lab plugin (me.tbsten.compose.preview.lab) must be applied " +
                    "BEFORE the Compose Compiler plugin ($composeCompilerPluginId) in build.gradle.kts. " +
                    "The current order may cause runtime errors with @Composable lambdas. " +
                    "Upgrading to Kotlin 2.3 or later removes this restriction automatically.",
            )
        }
    }
}
