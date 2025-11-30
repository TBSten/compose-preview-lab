package me.tbsten.compose.preview.lab.field

import androidx.compose.runtime.Composable
import me.tbsten.compose.preview.lab.PreviewLabScope

class WrapField<Value>(
    private val baseField: MutablePreviewLabField<Value>,
    private val content: @Composable (@Composable () -> Unit) -> Unit,
) : MutablePreviewLabField<Value>(label = baseField.label, initialValue = baseField.initialValue) {
    override var value: Value by baseField::value

    @Composable
    override fun Content() {
        content {
            baseField.Content()
        }
    }
}

fun <Value> MutablePreviewLabField<Value>.wrap(content: @Composable (@Composable () -> Unit) -> Unit,) = WrapField(
    baseField = this,
    content = content,
)

@Composable
fun PreviewLabScope.Test() {
    fieldValue {
        StringField("", "")
            .wrap { content ->
                content()
            }
    }
}
