package me.tbsten.compose.preview.lab.field

import androidx.compose.runtime.Composable

class StringField(
    label: String,
    initialValue: String,
    private val prefix: (@Composable () -> Unit)? = null,
    private val suffix: (@Composable () -> Unit)? = null,
) : MutablePreviewLabField<String>(
    label = label,
    initialValue = initialValue,
) {
    @Composable
    override fun Content() {
        TextFieldContent<String>(
            toValue = { Result.success(it) },
            toString = { it },
            prefix = prefix,
            suffix = suffix,
        )
    }
}
