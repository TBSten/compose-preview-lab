package me.tbsten.compose.preview.lab.generatecombinedfield

/**
 * Annotation to generate a CombinedField extension function for a data class.
 *
 * When applied to a data class with a companion object, this will generate
 * a `field` extension function on the companion object that creates a
 * MutablePreviewLabField<T> with CombinedField.
 *
 * ## Requirements
 *
 * The annotated class must satisfy the following requirements:
 * - Must be a `data class`
 * - Must have a `companion object`
 * - Must have a primary constructor
 * - Must have at least 1 property
 * - Must have at most 10 properties
 *
 * ## Example
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
 *
 * ## Supported Property Types
 *
 * - Primitive types: String, Int, Float, Boolean, etc.
 * - Compose value types: Dp, Color, etc.
 * - Nullable types
 * - Enum types
 * - Value classes
 * - Nested data classes with companion objects (recursive generation)
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class GenerateCombinedField
