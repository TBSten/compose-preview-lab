package me.tbsten.compose.preview.lab.me.field

import androidx.compose.runtime.Composable

class IntField(
    label: String,
    initialValue: Int,
    private val prefix: (@Composable () -> Unit)? = null,
    private val suffix: (@Composable () -> Unit)? = null,
) : PreviewLabField<Int>(
    label = label,
    initialValue = initialValue
) {
    @Composable
    override fun View() = TextFieldView(
        toString = {
            it.toString()
        },
        toValue = {
            runCatching {
                it.toInt()
            }
        },
        prefix = prefix,
        suffix = suffix,
    )
}
