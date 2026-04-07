package me.tbsten.compose.preview.lab

/**
 * Returns a [Lazy] that provides the list of `@Preview` functions collected from **this module only**.
 *
 * The Compose Preview Lab compiler plugin replaces the [Lazy] initializer at compile time
 * with the actual list of [CollectedPreview] instances. If the compiler plugin is not applied,
 * accessing the value throws an [IllegalStateException].
 *
 * The return type is [Lazy] so that `@Composable` lambdas inside [CollectedPreview] are
 * initialized on first access rather than at class-load time, avoiding `ExceptionInInitializerError`.
 *
 * Example — single-module preview collection:
 * ```kotlin
 * val myPreviews by collectModulePreviews()
 * ```
 *
 * Example — used in a library module with public visibility:
 * ```kotlin
 * // uiLib/src/commonMain/kotlin/Previews.kt
 * val uiLibPreviews by collectModulePreviews()
 * ```
 *
 * @return a [Lazy] wrapping the collected preview list; the initializer is replaced by the compiler plugin
 * @see collectAllModulePreviews
 */
fun collectModulePreviews(): Lazy<List<CollectedPreview>> = lazy {
    error(
        "[ComposePreviewLab] collectModulePreviews() was not replaced by the compiler plugin. " +
            "Make sure the Compose Preview Lab compiler plugin is applied to this module.",
    )
}

/**
 * Returns a [Lazy] that provides the list of `@Preview` functions collected from **this module
 * and all dependency modules** that export their preview properties.
 *
 * The compiler plugin replaces the [Lazy] initializer at compile time with a concatenation
 * of this module's previews and the values of dependency modules' exported preview properties.
 * Dependency modules are discovered via the `collectPreviewsExport` Gradle configuration.
 *
 * Example — app module aggregating its own and library previews:
 * ```kotlin
 * // app/src/commonMain/kotlin/Previews.kt
 * val appPreviews by collectAllModulePreviews()
 * ```
 *
 * Example — dependency module exporting its previews via Gradle:
 * ```kotlin
 * // uiLib/build.gradle.kts
 * composePreviewLab {
 *     collectPreviewsExport = "uiLib.uiLibPreviews"
 * }
 * ```
 *
 * @return a [Lazy] wrapping the aggregated preview list; the initializer is replaced by the compiler plugin
 * @see collectModulePreviews
 */
fun collectAllModulePreviews(): Lazy<List<CollectedPreview>> = lazy {
    error(
        "[ComposePreviewLab] collectAllModulePreviews() was not replaced by the compiler plugin. " +
            "Make sure the Compose Preview Lab compiler plugin is applied to this module.",
    )
}
