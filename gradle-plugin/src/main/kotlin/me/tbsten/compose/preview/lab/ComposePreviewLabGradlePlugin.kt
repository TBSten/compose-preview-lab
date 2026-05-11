@file:OptIn(ExperimentalComposePreviewLabApi::class)

package me.tbsten.compose.preview.lab

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * A Gradle plugin for configuring the Compose Preview Lab.
 *
 * On apply this plugin:
 * 1. Creates the [ComposePreviewLabExtension] (Project-injection-free; see its KDoc).
 * 2. Wires `gradle.properties` (and `-PcomposePreviewLab.*=...`) values as the
 *    **convention** of each DSL property, so the precedence chain
 *    `DSL block > gradle.properties > built-in default` (the Gradle norm) holds.
 * 3. Validates the `defaultCollectScope` value (`[A-Za-z0-9_]+`) early — at configuration
 *    time, before any source file is compiled — when the value comes from
 *    `gradle.properties`. A typo surfaces as a clear `GradleException` instead of being
 *    deferred until the compiler plugin rejects it during `compileKotlin`.
 * 4. Applies [ComposePreviewLabSubplugin] (the `KotlinCompilerPluginSupportPlugin` half).
 * 5. Emits a single, root-level warning for any `composePreviewLab.*` key that is not
 *    recognized (typo guard).
 */
class ComposePreviewLabGradlePlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = with(project) {
        val extension = extensions.create("composePreviewLab", ComposePreviewLabExtension::class.java)
        wireExtensionConventions(extension)
        pluginManager.apply(ComposePreviewLabSubplugin::class.java)
        configureFeaturedFiles(extension = extension)
        if (!supportsCompilerPluginOrder()) {
            validatePluginOrder()
        }
        warnOnUnknownComposePreviewLabProperties(project = this)
    }

    /**
     * Wires each `composePreviewLab.*` `gradle.properties` key as the **convention** for
     * its matching DSL property, so the DSL block still wins when set explicitly.
     *
     * `defaultCollectScope` is additionally validated **at configuration time** against
     * `[A-Za-z0-9_]+` (the same regex the compiler plugin enforces). Without this,
     * an invalid scope value silently propagates to `compileKotlin` and surfaces as a
     * `CliOptionProcessingException` only after the task graph has finished resolving,
     * which is needlessly late.
     */
    private fun Project.wireExtensionConventions(extension: ComposePreviewLabExtension) {
        extension.generatePackage.convention(
            stringProp(
                project = this,
                key = ComposePreviewLabProperties.GeneratePackage,
                default = name
                    .split(Regex("(\\.)|_|-"))
                    .mapIndexed { index, word -> if (index == 0) word else word.replaceFirstChar { it.uppercase() } }
                    .joinToString(""),
            ),
        )
        extension.projectRootPath.convention(
            stringProp(
                project = this,
                key = ComposePreviewLabProperties.ProjectRootPath,
                default = rootProject.projectDir.absolutePath,
            ),
        )
        extension.generateFeaturedFiles.convention(
            boolProp(
                project = this,
                key = ComposePreviewLabProperties.GenerateFeaturedFiles,
                default = false,
            ),
        )
        extension.collectPreviews.enabled.convention(
            boolProp(
                project = this,
                key = ComposePreviewLabProperties.CollectPreviewsEnabled,
                default = true,
            ),
        )
        extension.collectPreviews.defaultCollectScope.convention(
            stringProp(
                project = this,
                key = ComposePreviewLabProperties.CollectPreviewsDefaultCollectScope,
                default = ComposePreviewLabOption.DefaultCollectScope,
            ).map { value ->
                validateDefaultCollectScope(value)
                value
            },
        )
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

/**
 * Regex mirroring `ScopeIdentifierRegex` in the compiler plugin
 * (`compiler-plugin/.../ComposePreviewLabCommandLineProcessor.kt`). Kept duplicated
 * deliberately so the Gradle plugin can validate **without depending on the compiler
 * plugin module** (which would create an unwanted classpath dependency in user builds).
 */
private val DefaultCollectScopeRegex = Regex("[A-Za-z0-9_]+")

/**
 * Throws a clear [GradleException] when [value] would be rejected by the compiler
 * plugin's `OptionDefaultCollectScope` CLI option processor. Surfacing the error
 * at configuration time means users see it before the task graph finishes resolving,
 * not after a long `compileKotlin` run.
 */
private fun validateDefaultCollectScope(value: String) {
    if (!DefaultCollectScopeRegex.matches(value)) {
        throw GradleException(
            "[compose-preview-lab] Invalid value for '${ComposePreviewLabProperties.CollectPreviewsDefaultCollectScope}': " +
                "\"$value\". Expected a Kotlin-identifier-safe scope matching [A-Za-z0-9_]+. " +
                "Set this in gradle.properties or via `-P${ComposePreviewLabProperties.CollectPreviewsDefaultCollectScope}=...`.",
        )
    }
}
