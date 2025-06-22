package me.tbsten.compose.preview.lab.field

import androidx.compose.runtime.Composable

/**
 * A field that allows users to input a string value.
 *
 * ```kt
 * PreviewLab {
 *   Text(
 *     text = fieldValue { StringField("Text.text", "Hello World!!") }
 *   )
 * }
 * ```
 *
 * @param prefix The slot that will be displayed as the prefix of the TextField.
 * @param suffix The slot that will be displayed as the suffix of TextField.
 */
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
