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

open class ListField<Value>(
    label: String,
    initialValue: List<Value>,
    internal val elementField: ElementFieldScope.() -> MutablePreviewLabField<Value>,
    private val defaultValue: () -> Value = { initialValue.first() },
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
            fields.clear()
            value.forEachIndexed { index, value ->
                val oldField = fields.getOrNull(index)
                if (oldField != null) {
                    oldField.label = "$index"
                    oldField.value = value
                } else {
                    fields.add(elementField(ElementFieldScope("$index", value)))
                }
            }
        }

    internal fun insertAt(index: Int) {
        val newField = elementField(ElementFieldScope("$index", defaultValue()))
        fields.add(index, newField)
    }

    override fun serializer(): KSerializer<List<Value>>? = runCatching { serializer<List<Value>>() }.getOrNull()
    override fun valueCode(): String = "listOf(\n" +
        fields.joinToString(",\n") { "    ${it.valueCode()}" } +
        ")"

    @Composable
    override fun Content() {
        var isModalShow by remember { mutableStateOf(false) }

        CollectionFieldSummaryCard(
            summaryText = if (fields.isEmpty()) "(Empty)" else fields.joinToString(", ") { it.valueCode() },
            onClick = { isModalShow = !isModalShow },
        )

        CollectionFieldEditModal(
            label = label,
            fields = fields,
            isVisible = isModalShow,
            onDismissRequest = { isModalShow = false },
            onInsertAt = ::insertAt,
        )
    }

    inner class ElementFieldScope internal constructor(val label: String, val initialValue: Value)
}

fun <Value> MutablePreviewLabField<List<Value>>.withEmptyHint() = withHint("Empty List" to emptyList())
