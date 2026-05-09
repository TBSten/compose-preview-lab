package me.tbsten.compose.preview.lab

/**
 *
 * ### usages
 *
 * By default, the qualifiedName of the Preview (ex. com.example.my.buttons.MyButtonPreview) is used as the Preview name, but this can be overridden by displayName.
 * The name `MyButtonPreview` can be displayed on ComposePreviewLab by specifying the following.
 *
 * ```kt
 * package com.example.my.buttons
 *
 * @ComposePreviewLabOption(displayName = "{{simpleName}}")
 * @Preview
 * @Composable
 * fun MyButtonPreview() {
 *     MyButton()
 * }
 *
 * @ComposePreviewLabOption(displayName = "{{simpleName}}.Red")
 * @Preview
 * @Composable
 * fun MyButtonRedPreview() {
 *     MyButton(color = Color.Red)
 * }
 * ```
 *
 * Options such as displayName allow placeholders in the form `{{placeholderName}}`.
 * Supported placeholder meanings are as follows
 * - {{package}} ... the package name of the Preview, e.g. `com.example.my.buttons`.
 * - {{simpleName}} ... the simple name of the Preview, e.g. `MyButtonPreview`.
 * - {{qualifiedName}} ... the fully qualified name of the Preview, e.g. `com.example.my.buttons.MyButtonPreviewKt.MyButtonPreview` if the Preview is defined in a file named `MyButtonPreview.kt`.
 *
 * ### About `collectScopes`
 *
 * `collectScopes` exists primarily to **keep a component library's previews out of a
 * consumer application's gallery**. Most projects pick one scope per module once and
 * never override it per-`@Preview`; the multi-element form is supported, but it is the
 * minority case for previews that legitimately belong in more than one bucket.
 *
 * #### Primary use case — library / app isolation (1 module : 1 scope)
 *
 * A component library publishes its previews so its maintainers can iterate on them; the
 * library's downstream consumers (apps) do not want the library's previews showing up in
 * their own `collectAllModulePreviews()` call. The conventional pattern is one Gradle
 * DSL line in the library — every `@Preview` in that module is then automatically
 * registered under the library scope, with no per-`@Preview` annotation required:
 *
 * ```kotlin
 * // -- inside :ui-library / build.gradle.kts --
 * composePreviewLab {
 *     collectPreviews {
 *         defaultCollectScope = "acme_ui"
 *     }
 * }
 *
 * // -- inside :ui-library / Button.kt --
 * @Preview                                 // no @ComposePreviewLabOption needed
 * @Composable
 * fun PrimaryButtonPreview() { PrimaryButton() }
 * // ↑ emitted under previewHint_acme_ui because of the DSL above.
 *
 * // -- inside :ui-library / Previews.kt --
 * val libraryGallery by collectAllModulePreviews(scope = "acme_ui")
 *
 * // -- inside :app (depends on :ui-library) --
 * @Preview @Composable fun LoginScreenPreview() { LoginScreen() }   // → previewHint_default
 * val appGallery by collectAllModulePreviews()                      // sees LoginScreenPreview only
 *                                                                   // — PrimaryButtonPreview is hidden
 * ```                                                               //   under "acme_ui".
 *
 * The mechanism: the compiler plugin reads
 * `composePreviewLab.collectPreviews.defaultCollectScope` from the Gradle DSL and
 * substitutes any `[DefaultCollectScope]` (= `["default"]`) per-`@Preview` value with
 * it. So you can leave `@ComposePreviewLabOption.collectScopes` at its default
 * everywhere and still get module-level isolation.
 *
 * #### Secondary use case — one preview registered in multiple scopes
 *
 * When a single preview legitimately belongs in more than one bucket (e.g. a "showcase"
 * gallery that re-uses pieces from the library's own gallery), the per-`@Preview`
 * annotation form takes over and lists every scope explicitly:
 *
 * ```kotlin
 * @ComposePreviewLabOption(collectScopes = ["acme_ui", "showcase"])
 * @Preview @Composable
 * fun ButtonShowcase() { PrimaryButton() }
 *
 * val libraryGallery by collectAllModulePreviews(scope = "acme_ui")   // sees it
 * val showcaseGallery by collectAllModulePreviews(scope = "showcase") // also sees it
 * ```
 *
 * Each entry must match `[A-Za-z0-9_]+` because the compiler plugin embeds it into the
 * synthetic hint function name (`previewHint_<scope>`); the FIR
 * `CollectScopeAnnotationChecker` reports invalid values per element at analysis time,
 * before IR generation, so the error surfaces in the IDE highlighter.
 * `ignore = true` always wins, regardless of how many scopes are listed.
 *
 * @property displayName {{package}}, {{simpleName}}, {{qualifiedName}} or a custom string. It does not have to match other Previews as it does not function like an ID. `. Each segment separated by ` is considered a group.
 * @property ignore if true, Compose Preview Lab does not collect this Preview. The exclusion applies in both directions: the same module's `collectModulePreviews()` drops it via the IR-side filter, and consumer modules' `collectAllModulePreviews()` cannot discover it because no per-declaration hint is emitted in the first place.
 * @property id An ID to identify each Preview. It can be used for navigation within the PreviewLabNavController. The same placeholder as displayName can be used.
 * @property collectScopes The collection scopes this preview participates in. See the *About `collectScopes`* section above; the typical project picks a single scope per module via the Gradle DSL and leaves this at its default — explicit per-`@Preview` overrides are reserved for the rare multi-scope case.
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
annotation class ComposePreviewLabOption(
    val displayName: String = "{{qualifiedName}}",
    val ignore: Boolean = false,
    val id: String = "{{qualifiedName}}",
    @property:ExperimentalComposePreviewLabApi
    val collectScopes: Array<String> = [DefaultCollectScope],
) {
    public companion object {
        /**
         * The single source-of-truth string the whole `collectScope` system bottoms out on.
         *
         * It is used as:
         * - the default value of [collectScopes] (this annotation's `Array<String>` parameter),
         * - the default `scope` of [collectModulePreviews] / [collectAllModulePreviews] in
         *   user-facing call sites, and
         * - the default of `composePreviewLab.collectPreviews.defaultCollectScope` in the
         *   Gradle DSL (the per-module scope that the compiler plugin substitutes for any
         *   `[DefaultCollectScope]`-shaped per-preview / per-call value).
         *
         * Resolution order at compile time, per `@Preview` and per `collect[All]ModulePreviews`
         * call:
         * 1. If the explicit annotation argument or call-site argument is anything other than
         *    `[DefaultCollectScope]` / `DefaultCollectScope`, it wins as-is.
         * 2. Otherwise, the compiler plugin substitutes the module's
         *    `composePreviewLab.collectPreviews.defaultCollectScope` (set via the Gradle DSL)
         *    so a library can pin all of its previews to a library-specific bucket without
         *    annotating each `@Preview`.
         * 3. If the Gradle DSL was not set either, the runtime default `"default"` applies.
         */
        @ExperimentalComposePreviewLabApi
        public const val DefaultCollectScope: String = "default"
    }
}
