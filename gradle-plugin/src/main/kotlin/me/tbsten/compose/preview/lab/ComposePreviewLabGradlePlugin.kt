package me.tbsten.compose.preview.lab

import me.tbsten.compose.preview.lab.dokka.ComposePreviewLabDokkaIntegration
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
        registerDokkaPluginParameters()
    }

    /**
     * Dokka Gradle plugin (v2) が同じプロジェクトに適用されたら `dokka.pluginsConfiguration` に
     * `composePreviewLab { }` エントリを登録し、 Compose Preview Lab Dokka plugin
     * (`me.tbsten.compose.preview.lab:dokka-plugin`) を型安全に設定できるようにする。
     *
     * Dokka のクラスは Dokka plugin 適用後にしか触らない (別クラスに隔離 + `withPlugin`) ので、
     * Dokka を使わないプロジェクトではクラスロードすら発生しない。 Dokka plugin が別のクラスローダ
     * (別の buildscript classpath) にしか存在しない場合は `NoClassDefFoundError` になるため、
     * その場合は警告だけ出して処理を続行する。
     */
    private fun Project.registerDokkaPluginParameters() {
        pluginManager.withPlugin(ComposePreviewLabDokkaIntegration.DokkaGradlePluginId) {
            try {
                ComposePreviewLabDokkaIntegration.register(this)
            } catch (e: LinkageError) {
                logger.warn(
                    "Compose Preview Lab: could not register `dokka.pluginsConfiguration.composePreviewLab` in $path " +
                        "because the Dokka Gradle plugin classes are not visible from the Compose Preview Lab Gradle plugin " +
                        "(${e.javaClass.simpleName}: ${e.message}). Apply both plugins in the same `plugins {}` block, " +
                        "or add `org.jetbrains.dokka` to the buildscript classpath where `me.tbsten.compose.preview.lab` is declared.",
                )
            }
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
