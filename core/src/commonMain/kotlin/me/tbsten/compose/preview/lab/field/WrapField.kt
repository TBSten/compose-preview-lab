package me.tbsten.compose.preview.lab.field

import androidx.compose.runtime.Composable

class WrapField<Value>(
    private val baseField: MutablePreviewLabField<Value>,
    private val wrapRange: WrapRange = WrapRange.OnlyContent,
    private val content: @Composable (@Composable () -> Unit) -> Unit,
) : MutablePreviewLabField<Value>(label = baseField.label, initialValue = baseField.initialValue) {
    override var value: Value by baseField::value

    @Composable
    override fun View() {
        if (wrapRange.wrapView) {
            content {
                super.View()
            }
        } else {
            super.View()
        }
    }

    @Composable
    override fun Content() {
        if (wrapRange.wrapContent) {
            content {
                baseField.Content()
            }
        } else {
            baseField.Content()
        }
    }
}

fun <Value> MutablePreviewLabField<Value>.wrap(
    wrapRange: WrapRange = WrapRange.OnlyContent,
    content: @Composable (@Composable () -> Unit) -> Unit,
) = WrapField(
    baseField = this,
    wrapRange = wrapRange,
    content = content,
)

enum class WrapRange(internal val wrapView: Boolean, internal val wrapContent: Boolean) {
    Full(wrapView = true, wrapContent = false),
    OnlyContent(wrapView = false, wrapContent = true),
}
