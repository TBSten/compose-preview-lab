@file:OptIn(ExperimentalComposePreviewLabApi::class)

package me.tbsten.compose.preview.lab

import javax.inject.Inject
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.newInstance

/**
 * Configuration extension for Compose Preview Lab Gradle plugin
 *
 * Provides configuration options for customizing the code generation and preview collection
 * behavior of Compose Preview Lab. Applied in build.gradle.kts using the `composePreviewLab` block.
 *
 * ```kotlin
 * composePreviewLab {
 *     generatePackage.set("myModule")
 *     generateFeaturedFiles.set(true)
 *     collectPreviews {
 *         enabled.set(false)
 *     }
 * }
 * ```
 *
 * Every property below can also be set via `gradle.properties` (or `-PcomposePreviewLab.*=...`).
 * Precedence: DSL block > gradle.properties > built-in default.
 * See [ComposePreviewLabProperties] for the full key list.
 *
 * **Implementation note**: this extension intentionally injects **only**
 * [ObjectFactory], which is one of the Gradle-supported `@Inject` services.
 * The `gradle.properties` → convention wiring is performed by
 * [ComposePreviewLabGradlePlugin.apply] (which has full [org.gradle.api.Project] access)
 * rather than inside the extension constructor, because injecting `Project` into
 * managed extension types is **not supported** by Gradle and may break in future
 * versions (see Gradle service-injection docs).
 */
abstract class ComposePreviewLabExtension @Inject constructor(objects: ObjectFactory) {
    /**
     * Package name for generated preview lists
     *
     * Specifies the package where PreviewList and PreviewAllList objects will be generated.
     * Defaults to a camelCase version of the project name.
     *
     * Example: For project "my-app", defaults to "myApp"
     *
     * Can also be set via `gradle.properties`: `composePreviewLab.generatePackage=...`.
     */
    abstract val generatePackage: Property<String>

    /**
     * Root path of the project for file path resolution
     *
     * Used to resolve relative file paths in generated previews.
     * Defaults to the root project directory.
     *
     * Can also be set via `gradle.properties`: `composePreviewLab.projectRootPath=...`.
     */
    abstract val projectRootPath: Property<String>

    /**
     * Controls generation of FeaturedFileList from .composepreviewlab/featured/ directory
     *
     * When true, scans the .composepreviewlab/featured/ directory and generates
     * a FeaturedFileList map grouping file paths by directory name.
     * When false (default), no FeaturedFileList is generated.
     *
     * Can also be set via `gradle.properties`: `composePreviewLab.generateFeaturedFiles=true|false`.
     */
    abstract val generateFeaturedFiles: Property<Boolean>

    /**
     * Per-module configuration for the per-declaration preview hint pipeline.
     *
     * The defaults match the behavior of every existing build, so adding this DSL block
     * is opt-in. The most common knob is `enabled = false`, which **prevents this module
     * from emitting any preview hints** (no marker interface, no `previewHint_<scope>(...)`
     * overload), so its previews cannot leak into downstream `collectAllModulePreviews()`
     * consumers. As a deliberate policy gate, **`collectModulePreviews()` /
     * `collectAllModulePreviews()` call sites inside the same disabled module are
     * rejected at compile time**: a disabled module is declaring "I export no preview
     * surface", so a local collect call almost always indicates a configuration mistake
     * the build should surface up front. Use this knob for sample / test-fixture modules
     * whose previews must never leak into a downstream consumer or whose hint emission
     * cost is not justified. See [CollectPreviewsConfig] for the full set of options.
     */
    val collectPreviews: CollectPreviewsConfig = objects.newInstance<CollectPreviewsConfig>()

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
 *         enabled.set(false)
 *     }
 * }
 * ```
 *
 * Constructor takes only [ObjectFactory] (a Gradle-supported `@Inject` service);
 * convention values for [enabled] / [defaultCollectScope] are wired by
 * [ComposePreviewLabGradlePlugin.apply].
 */
abstract class CollectPreviewsConfig @Inject constructor(@Suppress("unused") objects: ObjectFactory) {
    /**
     * Whether this module participates in per-declaration preview hint emission.
     *
     * - `true` (default) — `@Preview` functions in this module emit a marker interface and
     *   a `previewHint_<scope>(...)` overload, so they can be discovered cross-module by
     *   `collectAllModulePreviews()` consumers.
     * - `false` — the compiler plugin emits no marker / hint pair for this module **and**
     *   any `collectModulePreviews()` / `collectAllModulePreviews()` call site in the same
     *   module becomes a compile-time error. Use this for sample / test-fixture modules
     *   whose previews should never leak into a downstream consumer.
     *
     * **Caveat — Binary Compatibility Validator interaction**: flipping this from `true`
     * (or default) to `false` deletes every `PreviewHintMarker_*` class and
     * `previewHint_*` overload from the generated bytecode. The Compose Preview Lab
     * compiler plugin stamps every synthesized hint with
     * `@InternalComposePreviewLabApi`, so any project that lists that marker in
     * `apiValidation { nonPublicMarkers }` (the recommended baseline configuration —
     * see this project's own root `build.gradle.kts`) already filters the hints out of
     * its `*.api` baselines. Toggling `enabled` is then ABI-neutral and `apiCheck`
     * stays green without further action.
     *
     * If your `apiValidation` block does **not** include
     * `me.tbsten.compose.preview.lab.InternalComposePreviewLabApi` in `nonPublicMarkers`,
     * either add it (recommended), or re-dump the baseline once after toggling
     * (`./gradlew apiDump`) so the new baseline records the absence of hints.
     */
    abstract val enabled: Property<Boolean>

    /**
     * Module-level default scope for `@Preview` hints emitted from this module.
     *
     * The compiler plugin substitutes any `@ComposePreviewLabOption(collectScopes = [...])` /
     * `collect[All]ModulePreviews(scope = ...)` value of `"default"` (the
     * `ComposePreviewLabOption.DefaultCollectScope` sentinel) with this string before
     * embedding it into the synthetic `previewHint_<scope>` function name. The typical
     * use is to pin a library module's previews to a library-specific bucket so they do
     * not leak into a consumer app's `collectAllModulePreviews()` call:
     *
     * ```kotlin
     * // uiLib/build.gradle.kts
     * composePreviewLab {
     *     collectPreviews {
     *         defaultCollectScope.set("acme_ui")
     *     }
     * }
     *
     * // uiLib/src/commonMain/kotlin/Button.kt
     * @Preview                      // no @ComposePreviewLabOption needed
     * @Composable
     * fun ButtonPreview() { Button() }
     * // ↑ emitted under previewHint_acme_ui because of the DSL above.
     * ```
     *
     * Defaults to `"default"` so existing builds keep producing `previewHint_default`
     * without any DSL change.
     *
     * The value must match `[A-Za-z0-9_]+` because it is embedded into a Kotlin identifier;
     * the Gradle plugin validates this **early** (at configuration time) for values coming
     * from `gradle.properties` / `-P`, and the compiler plugin's command-line processor
     * provides a second-layer guard for DSL-set values.
     *
     * **Experimental**: this knob is part of the still-stabilizing collectScopes design and
     * may change shape (or move under a different DSL block) before stable release. Opt in
     * with `@OptIn(ExperimentalComposePreviewLabApi::class)` at the consuming Kotlin source
     * site; the Gradle DSL itself does not enforce the opt-in (Gradle scripts have no
     * Kotlin opt-in propagation), but the annotation surfaces a warning in the IDE for
     * direct programmatic access.
     */
    @ExperimentalComposePreviewLabApi
    abstract val defaultCollectScope: Property<String>
}
