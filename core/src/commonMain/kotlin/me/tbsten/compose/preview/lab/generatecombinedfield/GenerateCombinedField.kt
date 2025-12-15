package me.tbsten.compose.preview.lab.generatecombinedfield

/**
 * Annotation to generate a field extension function for a data class or sealed interface/class.
 *
 * ## For Data Classes
 *
 * When applied to a data class with a companion object, this will generate
 * a `field` extension function on the companion object that creates a
 * MutablePreviewLabField<T> with CombinedField.
 *
 * ### Requirements for Data Classes
 *
 * - Must be a `data class`
 * - Must have a `companion object`
 * - Must have a primary constructor
 * - Must have at least 1 property
 * - Must have at most 10 properties
 *
 * ### Example for Data Classes
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
 * ## For Sealed Interfaces/Classes
 *
 * When applied to a sealed interface or sealed class with a companion object,
 * this will generate a `field` extension function that creates a
 * MutablePreviewLabField<T> with PolymorphicField, automatically detecting all
 * subclasses and generating appropriate fields for each.
 *
 * ### Requirements for Sealed Types
 *
 * - Must be a `sealed interface` or `sealed class`
 * - Must have a `companion object`
 * - All direct subclasses must be:
 *   - `data class` (will generate CombinedField)
 *   - `data object` or `object` (will generate FixedField)
 *
 * ### Example for Sealed Interfaces
 *
 * ```
 * @GenerateCombinedField
 * sealed interface UiState {
 *     data object Loading : UiState
 *     data class Success(val data: String) : UiState
 *     data class Error(val message: String) : UiState
 *     companion object
 * }
 *
 * // Generates:
 * fun UiState.Companion.field(label: String, initialValue: UiState): MutablePreviewLabField<UiState> =
 *     PolymorphicField(
 *         label = label,
 *         initialValue = initialValue,
 *         fields = listOf(
 *             FixedField("Loading", UiState.Loading),
 *             combined(label = "Success", ...) { UiState.Success(it) },
 *             combined(label = "Error", ...) { UiState.Error(it) },
 *         ),
 *     )
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
