package me.tbsten.compose.preview.lab.generatecombinedfield

/**
 * Annotation to generate a CombinedField extension function for a data class.
 *
 * When applied to a data class with a companion object, this will generate
 * a `field` extension function on the companion object that creates a
 * MutablePreviewLabField<T> with CombinedField.
 *
 * Example:
 *
 * ```
 * @GenerateCombinedField
 * data class MyUiState(val str: String, val int: Int, val bool: Boolean) {
 *     companion object
 * }
 *
 * // Generates:
 * fun MyUiState.Companion.field(label: String, initialValue: MyUiState): MutablePreviewLabField<MyUiState> = ...
 * ```
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class GenerateCombinedField
