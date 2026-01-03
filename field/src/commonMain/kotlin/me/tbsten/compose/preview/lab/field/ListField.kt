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
 * A field for editing [List] values with support for dynamic element insertion and deletion.
 *
 * ListField provides a UI for editing list collections where each element is represented
 * by its own field. Elements can be inserted at any position using insert buttons,
 * and the field automatically maintains correct indices.
 *
 * # Usage
 *
 * ```kotlin
 * PreviewLab {
 *     val characters by fieldValue {
 *         ListField(
 *             label = "characters",
 *             initialValue = listOf("Alice", "Bob", "Charlie"),
 *             elementField = { StringField(label, initialValue) },
 *         )
 *     }
 *     Text(characters.joinToString(", "))
 * }
 * ```
 *
 * @param Value The type of elements in the list.
 * @param label Label displayed in the UI for this field.
 * @param initialValue Initial list value.
 * @param elementField Factory function to create a field for each element.
 *                     Receives [ElementFieldScope] with the element's label (index) and initial value.
 * @param defaultValue Factory function to create a default value when inserting new elements.
 *                     Defaults to the first element of initialValue if available.
 */
open class ListField<Value>(
    label: String,
    initialValue: List<Value>,
    internal val elementField: ElementFieldScope.() -> MutablePreviewLabField<Value>,
    private val defaultValue: () -> Value = {
        initialValue.firstOrNull()
            ?: error("ListField requires a non-empty initialValue or an explicit defaultValue")
    },
) : MutablePreviewLabField<List<Value>>(
    label = label,
    initialValue = initialValue,
) {
    internal val fields =
        initialValue
            .mapIndexed { index, value ->
                elementField(ElementFieldScope("$index", value))
            }.toMutableStateList()

    private val _value by derivedStateOf { fields.map { it.value } }
    override var value: List<Value>
        get() = _value
        set(value) {
            // Update existing fields or add new ones to match the new value list
            value.forEachIndexed { index, newValue ->
                val oldField = fields.getOrNull(index)
                if (oldField != null) {
                    oldField.label = "$index"
                    oldField.value = newValue
                } else {
                    fields.add(elementField(ElementFieldScope("$index", newValue)))
                }
            }
            // Remove extra fields if the new value list is shorter
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

    override fun serializer(): KSerializer<List<Value>>? = runCatching { serializer<List<Value>>() }.getOrNull()
    override fun valueCode(): String = "listOf(\n" + fields.joinToString(",\n") { "    ${it.valueCode()}" } + ")"

    @Composable
    override fun Content() {
        var isModalShow by remember { mutableStateOf(false) }

        val summaryText = remember(fields.size) {
            if (fields.isEmpty()) {
                "(Empty)"
            } else {
                val displayed = fields.take(SummaryMaxItems).joinToString(", ") { it.valueCode() }
                val remaining = fields.size - SummaryMaxItems
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
        )
    }

    /**
     * Scope provided to [elementField] factory function for creating element fields.
     *
     * @property label The label for the element field, representing its index in the list.
     * @property initialValue The initial value for the element.
     */
    inner class ElementFieldScope internal constructor(val label: String, val initialValue: Value)
}

/**
 * Adds an "Empty List" hint to a [ListField], allowing quick selection of an empty list value.
 *
 * @return The field with the empty list hint added.
 */
fun <Value> MutablePreviewLabField<List<Value>>.withEmptyHint() = withHint("Empty List" to emptyList())
