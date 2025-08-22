package me.tbsten.compose.preview.lab.field

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString

class AnnotatedStringField(
    label: String,
    initialValue: AnnotatedString,
) : MutablePreviewLabField<AnnotatedString>(
    label = label,
    initialValue = initialValue,
) {
    @Composable
    override fun Content() {
        TextFieldContent(
            toString = { it.text },
            toValue = {
                runCatching {
                    AnnotatedString(
                        text = it,
                        spanStyles = value.spanStyles,
                        paragraphStyles = value.paragraphStyles,
                    )
                }
            },
        )
    }
}
