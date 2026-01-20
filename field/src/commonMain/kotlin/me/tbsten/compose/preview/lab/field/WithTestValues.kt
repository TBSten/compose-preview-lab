package me.tbsten.compose.preview.lab.field

import androidx.compose.runtime.Composable
import kotlinx.serialization.KSerializer
import me.tbsten.compose.preview.lab.MutablePreviewLabField
import me.tbsten.compose.preview.lab.PreviewLabField
import me.tbsten.compose.preview.lab.PreviewLabField.ViewMenuItem

public class WithTestValuesField<Value>(
    public val baseField: PreviewLabField<Value>,
    private vararg val additionalTestValues: Value
) : PreviewLabField<Value> by baseField {
    override fun testValues(): List<Value> = super.testValues() + additionalTestValues
    override fun serializer(): KSerializer<Value>? = baseField.serializer()
}

public class MutableWithTestValuesField<Value>(
    public val baseField: MutablePreviewLabField<Value>,
    private vararg val additionalTestValues: Value,
) : MutablePreviewLabField<Value>(
    label = baseField.label,
    initialValue = baseField.initialValue,
) {
    override var value: Value by baseField::value
    override fun testValues(): List<Value> = baseField.testValues() + additionalTestValues
    override fun valueCode(): String = baseField.valueCode()
    override fun serializer(): KSerializer<Value>? = baseField.serializer()

    @Composable
    override fun View(menuItems: List<ViewMenuItem<Value>>): Unit = baseField.View(menuItems = menuItems)

    @Composable
    override fun Content(): Unit = baseField.Content()
}

public fun <Value> PreviewLabField<Value>.withTestValues(vararg additionalTestValues: Value): WithTestValuesField<Value> =
    WithTestValuesField(
        baseField = this,
        additionalTestValues = additionalTestValues,
    )

public fun <Value> MutablePreviewLabField<Value>.withTestValues(
    vararg additionalTestValues: Value
): MutableWithTestValuesField<Value> = MutableWithTestValuesField(
    baseField = this,
    additionalTestValues = additionalTestValues,
)
