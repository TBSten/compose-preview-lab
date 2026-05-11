package me.tbsten.compose.preview.lab

/**
 * Per-`@Preview` options for Compose Preview Lab.
 *
 * By default the preview's qualified name (e.g. `com.example.my.buttons.MyButtonPreview`)
 * is used as both `displayName` and `id`. Override with a placeholder template:
 *
 * ```kt
 * package com.example.my.buttons
 *
 * @ComposePreviewLabOption(displayName = "{{simpleName}}")
 * @Preview @Composable
 * fun MyButtonPreview() { MyButton() }
 *
 * @ComposePreviewLabOption(displayName = "{{simpleName}}.Red")
 * @Preview @Composable
 * fun MyButtonRedPreview() { MyButton(color = Color.Red) }
 * ```
 *
 * Supported placeholders in `displayName` / `id`:
 * - `{{package}}` — preview's package name (e.g. `com.example.my.buttons`).
 * - `{{simpleName}}` — preview's simple name (e.g. `MyButtonPreview`).
 * - `{{qualifiedName}}` — preview's fully qualified name (e.g.
 *   `com.example.my.buttons.MyButtonPreviewKt.MyButtonPreview`).
 *
 * `collectScopes` controls which `collect[All]ModulePreviews(scope = ...)` call picks up
 * this preview. Most projects pick one scope per module via the Gradle DSL and leave this
 * argument at its default. See
 * [annotation/docs/collect-scopes.md](https://github.com/TBSten/compose-preview-lab/blob/main/annotation/docs/collect-scopes.md)
 * for the full design rationale, primary / secondary use cases, and BCV-visibility quirks
 * of the experimental marker.
 *
 * @property displayName Placeholder template or custom string. Does not have to be unique
 *   (not used as an ID). Segments separated by `.` are treated as groups.
 * @property ignore If true, drops this preview from collection. The exclusion is bidirectional:
 *   the same module's `collectModulePreviews()` filters it out at IR time, and consumer
 *   modules' `collectAllModulePreviews()` cannot discover it (no per-declaration hint is
 *   emitted).
 * @property id Used for navigation within `PreviewLabNavController`. Accepts the same
 *   placeholders as `displayName`.
 * @property collectScopes Collection scopes this preview participates in. Defaults to the
 *   module-level `composePreviewLab.collectPreviews.defaultCollectScope` (substituted by
 *   the compiler plugin at compile time).
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
    /**
     * Companion holding [DefaultCollectScope]. Marked `@ExperimentalComposePreviewLabApi`
     * so the empty `Companion` class itself stays out of BCV baselines if the const ever
     * moves — see the *Experimental marker visibility* section of
     * [annotation/docs/collect-scopes.md](https://github.com/TBSten/compose-preview-lab/blob/main/annotation/docs/collect-scopes.md).
     */
    @ExperimentalComposePreviewLabApi
    public companion object {
        /**
         * Sentinel default for [collectScopes], `collectModulePreviews(scope = ...)`,
         * `collectAllModulePreviews(scope = ...)`, and the
         * `composePreviewLab.collectPreviews.defaultCollectScope` Gradle DSL. The compiler
         * plugin substitutes this with the module's Gradle DSL value at compile time,
         * falling back to `"default"` at runtime if the DSL is unset.
         *
         * Carries `@ExperimentalComposePreviewLabApi` independently of the surrounding
         * `Companion` so the flattened `public static final field` on JVM / Android stays
         * out of BCV baselines too (the companion-level annotation does not propagate to
         * that field). See
         * [annotation/docs/collect-scopes.md](https://github.com/TBSten/compose-preview-lab/blob/main/annotation/docs/collect-scopes.md)
         * for the full resolution semantics and BCV rationale.
         */
        @ExperimentalComposePreviewLabApi
        public const val DefaultCollectScope: String = "default"
    }
}
