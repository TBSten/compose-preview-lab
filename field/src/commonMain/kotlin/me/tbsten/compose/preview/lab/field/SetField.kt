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
    private val defaultValue: () -> Value = { initialValue.first() },
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
            fields.clear()
            value.forEachIndexed { index, v ->
                val oldField = fields.getOrNull(index)
                if (oldField != null) {
                    oldField.label = "$index"
                    oldField.value = v
                } else {
                    fields.add(elementField(ElementFieldScope("$index", v)))
                }
            }
        }

    internal fun insertAt(index: Int) {
        val newField = elementField(ElementFieldScope("$index", defaultValue()))
        fields.add(index, newField)
    }

    override fun serializer(): KSerializer<Set<Value>>? = runCatching { serializer<Set<Value>>() }.getOrNull()
    override fun valueCode(): String = "setOf(\n" +
        fields.joinToString(",\n") { "    ${it.valueCode()}" } +
        ")"

    @Composable
    override fun Content() {
        var isModalShow by remember { mutableStateOf(false) }

        // summaryText は重複を除外して表示
        val uniqueValues = fields.map { it.value }.distinct()
        val summaryText = if (uniqueValues.isEmpty()) "(Empty)" else uniqueValues.joinToString(", ") { it.toString() }

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
            isDuplicate = { field ->
                fields.count { it.value == field.value } > 1
            },
        )
    }

    inner class ElementFieldScope internal constructor(val label: String, val initialValue: Value)
}

fun <Value> MutablePreviewLabField<Set<Value>>.withEmptyHint() = withHint("Empty Set" to emptySet())
