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

open class SetField<Value>(
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

    internal fun insertAt(index: Int) {
        val newField = elementField(ElementFieldScope("$index", defaultValue()))
        fields.add(index, newField)
        // Update labels of all fields to reflect new indices
        fields.forEachIndexed { i, field -> field.label = "$i" }
    }

    internal fun updateLabelsAfterDelete() {
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
            val uniqueValues = mutableListOf<Value>()
            for (field in fields) {
                if (seenValues.add(field.value)) {
                    uniqueValues.add(field.value)
                }
            }
            if (uniqueValues.isEmpty()) {
                "(Empty)"
            } else {
                val maxItems = 5
                val displayed = uniqueValues.take(maxItems).joinToString(", ") { it.toString() }
                val remaining = uniqueValues.size - maxItems
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

    inner class ElementFieldScope internal constructor(val label: String, val initialValue: Value)
}

fun <Value> MutablePreviewLabField<Set<Value>>.withEmptyHint() = withHint("Empty Set" to emptySet())
