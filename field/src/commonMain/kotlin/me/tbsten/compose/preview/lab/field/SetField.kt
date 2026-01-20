package me.tbsten.compose.preview.lab.field

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import me.tbsten.compose.preview.lab.MutablePreviewLabField
import me.tbsten.compose.preview.lab.field.component.CollectionFieldEditModal
import me.tbsten.compose.preview.lab.field.component.CollectionFieldSummaryCard

/** Maximum number of items to display in the summary view. */
private const val SummaryMaxItems = 5

/**
 * A field for editing [Set] values with support for dynamic element insertion, deletion, and duplicate detection.
 *
 * SetField provides a UI for editing set collections where each element is represented
 * by its own field. Elements can be inserted at any position using insert buttons.
 * Unlike [ListField], SetField automatically detects and highlights duplicate values,
 * displaying them with an error indicator in the edit modal.
 *
 * # Usage
 *
 * ```kotlin
 * PreviewLab {
 *     val fruits by fieldValue {
 *         SetField(
 *             label = "fruits",
 *             initialValue = setOf("Apple", "Banana", "Cherry"),
 *             elementField = { StringField(label, initialValue) },
 *         )
 *     }
 *     Text(fruits.joinToString(", "))
 * }
 * ```
 *
 * # Duplicate Detection
 *
 * When editing, if two or more elements have the same value, they will be highlighted
 * with an error color and a message indicating which values are duplicated.
 * The summary view shows only unique values.
 *
 * @param Value The type of elements in the set.
 * @param label Label displayed in the UI for this field.
 * @param initialValue Initial set value.
 * @param elementField Factory function to create a field for each element.
 *                     Receives [ElementFieldScope] with the element's label (index) and initial value.
 * @param defaultValue Factory function to create a default value when inserting new elements.
 *                     Defaults to the first element of initialValue if available.
 */
public open class SetField<Value>(
    label: String,
    initialValue: Set<Value>,
    internal val elementField: ElementFieldScope.() -> MutablePreviewLabField<Value>,
    private val defaultValue: () -> Value = {
        initialValue.firstOrNull()
            ?: error("SetField requires a non-empty initialValue or an explicit defaultValue")
    },
) : MutablePreviewLabField<Set<Value>>(
    label = label,
    initialValue = initialValue,
) {
    internal val fields =
        initialValue
            .mapIndexed { index, value ->
                elementField(ElementFieldScope("$index", value))
            }.toMutableStateList()

    private val _value by derivedStateOf { fields.map { it.value }.toSet() }
    override var value: Set<Value>
        get() = _value
        set(value) {
            // Update existing fields or add new ones to match the new value set
            value.forEachIndexed { index, newValue ->
                val oldField = fields.getOrNull(index)
                if (oldField != null) {
                    oldField.label = "$index"
                    oldField.value = newValue
                } else {
                    fields.add(elementField(ElementFieldScope("$index", newValue)))
                }
            }
            // Remove extra fields if the new value set is smaller
            while (fields.size > value.size) {
                fields.removeAt(fields.lastIndex)
            }
        }

    private fun insertAt(index: Int) {
        val newField = elementField(ElementFieldScope("$index", defaultValue()))
        fields.add(index, newField)
        // Update labels of all fields to reflect new indices
        fields.forEachIndexed { i, field -> field.label = "$i" }
    }

    private fun updateLabelsAfterDelete() {
        fields.forEachIndexed { i, field -> field.label = "$i" }
    }

    override fun serializer(): KSerializer<Set<Value>>? = runCatching { serializer<Set<Value>>() }.getOrNull()
    override fun valueCode(): String = "setOf(\n" + fields.joinToString(",\n") { "    ${it.valueCode()}" } + ")"

    @Composable
    override fun Content() {
        var isModalShow by remember { mutableStateOf(false) }

        // Display summaryText with duplicates removed and limit displayed items
        val summaryText = remember(fields.size) {
            val seenValues = mutableSetOf<Value>()
            val uniqueFields = mutableListOf<MutablePreviewLabField<Value>>()
            for (field in fields) {
                if (seenValues.add(field.value)) {
                    uniqueFields.add(field)
                }
            }
            if (uniqueFields.isEmpty()) {
                "(Empty)"
            } else {
                val displayed = uniqueFields.take(SummaryMaxItems).joinToString(", ") { it.valueCode() }
                val remaining = uniqueFields.size - SummaryMaxItems
                if (remaining > 0) "$displayed, ... and $remaining more" else displayed
            }
        }

        CollectionFieldSummaryCard(
            summaryText = summaryText,
            onClick = { isModalShow = !isModalShow },
        )

        CollectionFieldEditModal(
            label = label,
            fields = fields,
            isVisible = isModalShow,
            onDismissRequest = { isModalShow = false },
            onInsertAt = ::insertAt,
            onAfterDelete = ::updateLabelsAfterDelete,
            isDuplicate = { field ->
                fields.count { it.value == field.value } > 1
            },
        )
    }

    /**
     * Scope provided to [elementField] factory function for creating element fields.
     *
     * @property label The label for the element field, representing its index in the set.
     * @property initialValue The initial value for the element.
     */
    public inner class ElementFieldScope internal constructor(public val label: String, public val initialValue: Value)
}

/**
 * Adds an "Empty Set" hint to a [SetField], allowing quick selection of an empty set value.
 *
 * @return The field with the empty set hint added.
 */
public fun <Value> MutablePreviewLabField<Set<Value>>.withEmptyHint(): MutablePreviewLabField<Set<Value>> =
    withHint("Empty Set" to emptySet())
