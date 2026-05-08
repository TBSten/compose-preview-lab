package me.tbsten.compose.preview.lab

import javax.inject.Inject
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.setValue

/**
 * Configuration extension for Compose Preview Lab Gradle plugin
 *
 * Provides configuration options for customizing the code generation and preview collection
 * behavior of Compose Preview Lab. Applied in build.gradle.kts using the `composePreviewLab` block.
 *
 * ```kotlin
 * composePreviewLab {
 *     generatePackage = "myModule"
 *     generateFeaturedFiles = true
 *     collectPreviews {
 *         enabled = false
 *     }
 * }
 * ```
 */
abstract class ComposePreviewLabExtension @Inject constructor(objects: ObjectFactory, project: Project) {
    /**
     * Package name for generated preview lists
     *
     * Specifies the package where PreviewList and PreviewAllList objects will be generated.
     * Defaults to a camelCase version of the project name.
     *
     * Example: For project "my-app", defaults to "myApp"
     */
    var generatePackage: String by objects.property<String>()
        .convention(
            project.name
                .split(Regex("(\\.)|_|-"))
                .mapIndexed { index, word -> if (index == 0) word else word.replaceFirstChar { it.uppercase() } }
                .joinToString(""),
        )

    /**
     * Root path of the project for file path resolution
     *
     * Used to resolve relative file paths in generated previews.
     * Defaults to the root project directory.
     */
    var projectRootPath: String by objects.property<String>()
        .convention(project.rootProject.projectDir.absolutePath)

    /**
     * Controls generation of FeaturedFileList from .composepreviewlab/featured/ directory
     *
     * When true, scans the .composepreviewlab/featured/ directory and generates
     * a FeaturedFileList map grouping file paths by directory name.
     * When false (default), no FeaturedFileList is generated.
     */
    var generateFeaturedFiles: Boolean by objects.property<Boolean>()
        .convention(false)

    /**
     * Per-module configuration for the per-declaration preview hint pipeline.
     *
     * The defaults match the behaviour of every existing build, so adding this DSL block
     * is opt-in. See [CollectPreviewsConfig] for the available knobs.
     */
    val collectPreviews: CollectPreviewsConfig = objects.newInstance(CollectPreviewsConfig::class.java)

    /** Kotlin-DSL friendly accessor for [collectPreviews]. */
    fun collectPreviews(action: Action<CollectPreviewsConfig>) {
        action.execute(collectPreviews)
    }
}

/**
 * Configures the per-declaration preview hint pipeline for one module.
 *
 * ```kotlin
 * composePreviewLab {
 *     collectPreviews {
 *         enabled = false
 *     }
 * }
 * ```
 */
abstract class CollectPreviewsConfig @Inject constructor(objects: ObjectFactory) {
    /**
     * Whether this module participates in per-declaration preview hint emission.
     *
     * - `true` (default) — `@Preview` functions in this module emit a marker interface and
     *   a `previewHint(...)` overload, so they can be discovered cross-module by
     *   `collectAllModulePreviews()` consumers.
     * - `false` — the compiler plugin emits no marker / hint pair for this module **and**
     *   any `collectModulePreviews()` / `collectAllModulePreviews()` call site in the same
     *   module becomes a compile-time error. Use this for sample / test-fixture modules
     *   whose previews should never leak into a downstream consumer.
     */
    var enabled: Boolean by objects.property<Boolean>()
        .convention(true)
}
