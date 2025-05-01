package me.tbsten.compose.preview.lab.me.field

import androidx.compose.runtime.Composable

class IntField(
    label: String,
    initialValue: Int,
    private val inputType: InputType = InputType.TextField(),
) : PreviewLabField<Int>(
    label = label,
    initialValue = initialValue
) {
    @Composable
    override fun Content() = when (inputType) {
        is InputType.TextField -> TextFieldContent(
            toString = {
                it.toString()
            },
            toValue = {
                runCatching {
                    it.toInt()
                }
            },
            prefix = inputType.prefix,
            suffix = inputType.suffix,
        )

        is InputType.Slider -> TODO("InputType.Slider")
    }

    sealed interface InputType {
        data class TextField(
            val prefix: (@Composable () -> Unit)? = null,
            val suffix: (@Composable () -> Unit)? = null,
        ) : InputType

        data class Slider(
            val min: Int,
            val max: Int,
        ) : InputType

    }
}
