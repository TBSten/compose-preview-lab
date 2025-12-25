package me.tbsten.compose.preview.lab.field

import me.tbsten.compose.preview.lab.MutablePreviewLabField

/**
 * A field for enum values that displays all enum entries as choices.
 *
 * # Usage
 *
 * ```kotlin
 * enum class Priority { LOW, MEDIUM, HIGH }
 *
 * @Preview
 * @Composable
 * fun PriorityPreview() = PreviewLab {
 *     val priority: Priority = fieldValue {
 *         EnumField(
 *             label = "Priority",
 *             initialValue = Priority.MEDIUM,
 *         )
 *     }
 *     Text("Priority: $priority")
 * }
 * ```
 *
 * @param E The enum type
 * @param label The display label for this field
 * @param initialValue The initial enum value
 * @param entries All enum entries (automatically provided via reified type)
 */
inline fun <reified E : Enum<E>> enumField(label: String, initialValue: E,): MutablePreviewLabField<E> = SelectableField(
    label = label,
    choices = enumValues<E>().toList(),
    initialValue = initialValue,
    choiceLabel = { it.name },
    valueCode = { "${E::class.simpleName}.${it.name}" },
)

/**
 * A field for enum values with the first entry as default.
 *
 * # Usage
 *
 * ```kotlin
 * enum class Theme { LIGHT, DARK, AUTO }
 *
 * @Preview
 * @Composable
 * fun ThemePreview() = PreviewLab {
 *     val theme: Theme = fieldValue {
 *         EnumField<Theme>(label = "Theme")
 *     }
 *     Text("Theme: $theme")
 * }
 * ```
 *
 * @param E The enum type
 * @param label The display label for this field
 */
inline fun <reified E : Enum<E>> enumField(label: String): MutablePreviewLabField<E> {
    val entries = enumValues<E>().toList()
    return SelectableField(
        label = label,
        choices = entries,
        initialValue = entries.first(),
        choiceLabel = { it.name },
        valueCode = { "${E::class.simpleName}.${it.name}" },
    )
}
