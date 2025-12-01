package me.tbsten.compose.preview.lab

/**
 * **This is an internal annotation for Compose Preview Lab. Don't use this api manually.**
 *
 * Marks a module to aggregate its previews into the PreviewAllList
 *
 * When applied to a module, this annotation instructs the KSP plugin to include
 * the module's previews in the aggregated PreviewAllList object. This is used
 * internally by Compose Preview Lab to collect previews from multiple modules
 * into a single comprehensive list.
 *
 * Example usage in a build.gradle.kts file:
 * ```kotlin
 * ksp {
 *     arg("composePreviewLab.aggregate", "true")
 * }
 * ```
 */
@InternalComposePreviewLabApi
annotation class AggregateToAll
