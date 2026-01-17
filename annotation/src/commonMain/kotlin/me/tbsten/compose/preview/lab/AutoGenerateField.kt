package me.tbsten.compose.preview.lab

/**
 * Specifies a type for which a PreviewLabField factory function should be auto-generated.
 *
 * Apply this annotation to an object declaration to generate extension functions that create
 * PreviewLabField instances for the specified type. Multiple annotations can be applied to
 * generate factories for different types.
 *
 * ## Example Usage
 *
 * ```kotlin
 * @AutoGenerateField<String>
 * @AutoGenerateField<Int>(name = "integer")
 * @AutoGenerateField<MyUiState>
 * object Fields
 * ```
 *
 * This generates:
 * - `Fields.string(label: String, initialValue: String): PreviewLabField<String>`
 * - `Fields.integer(label: String, initialValue: Int): PreviewLabField<Int>`
 * - `Fields.myUiState(label: String, initialValue: MyUiState, ...): PreviewLabField<MyUiState>`
 *
 * ## Type Support
 *
 * The following types are supported:
 * - **Primitive types**: String, Int, Long, Float, Double, Boolean, Byte
 * - **Enum classes**: Generates EnumField
 * - **Object/Data Object**: Generates FixedField
 * - **Data classes**: Generates CombinedField with child field factories
 * - **Sealed interfaces/classes**: Generates PolymorphicField
 *
 * @param T The type for which to generate the factory function
 */
@ExperimentalComposePreviewLabApi
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@Repeatable
annotation class AutoGenerateField<T : Any>(
    /**
     * The name of the generated factory function.
     *
     * If empty (default), the name is derived from the type name in lowerCamelCase.
     * For example, `MyUiState` becomes `myUiState`.
     */
    val name: String = "",

    /**
     * When true, the generated factory function's `label` parameter will have
     * a default value derived from the type name.
     *
     * For example, with `@AutoGenerateField<MyUiState>(autoLabelByTypeName = true)`:
     * ```kotlin
     * fun Fields.myUiState(
     *     label: String = "myUiState",  // default value added
     *     initialValue: MyUiState,
     * ): PreviewLabField<MyUiState>
     * ```
     */
    val autoLabelByTypeName: Boolean = false,
)
