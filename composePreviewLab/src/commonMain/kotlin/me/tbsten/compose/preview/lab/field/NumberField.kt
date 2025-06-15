package me.tbsten.compose.preview.lab.field

import androidx.compose.runtime.Composable

abstract class NumberField<Num : Number>(
    label: String,
    initialValue: Num,
    private val fromString: (String) -> Num,
    private val toString: (Num) -> String = { it.toString() },
    private val inputType: InputType = InputType.TextField(),
) : MutablePreviewLabField<Num>(
    label = label,
    initialValue = initialValue
) {
    @Composable
    override fun Content() = when (inputType) {
        is InputType.TextField -> TextFieldContent(
            toString = {
                toString(it)
            },
            toValue = {
                runCatching {
                    fromString(it)
                }
            },
            prefix = inputType.prefix,
            suffix = inputType.suffix,
        )
        is InputType.Slider -> TODO("InputType.Slider")
    }

    sealed interface InputType {
        data class TextField(val prefix: (@Composable () -> Unit)? = null, val suffix: (@Composable () -> Unit)? = null,) :
            InputType

        data class Slider(val min: Int, val max: Int,) : InputType
    }
}

open class IntField(label: String, initialValue: Int, inputType: InputType = InputType.TextField(),) :
    NumberField<Int>(
        label = label,
        initialValue = initialValue,
        fromString = { it.toInt() },
        toString = { it.toString() },
        inputType = inputType,
    )

open class LongField(label: String, initialValue: Long, inputType: InputType = InputType.TextField(),) :
    NumberField<Long>(
        label = label,
        initialValue = initialValue,
        fromString = { it.toLong() },
        toString = { it.toString() },
        inputType = inputType,
    )

open class ByteField(label: String, initialValue: Byte, inputType: InputType = InputType.TextField(),) :
    NumberField<Byte>(
        label = label,
        initialValue = initialValue,
        fromString = { it.toByte() },
        toString = { it.toString() },
        inputType = inputType,
    )

open class DoubleField(label: String, initialValue: Double, inputType: InputType = InputType.TextField(),) :
    NumberField<Double>(
        label = label,
        initialValue = initialValue,
        fromString = { it.toDouble() },
        toString = { it.toString() },
        inputType = inputType,
    )

open class FloatField(label: String, initialValue: Float, inputType: InputType = InputType.TextField(),) :
    NumberField<Float>(
        label = label,
        initialValue = initialValue,
        fromString = { it.toFloat() },
        toString = { it.toString() },
        inputType = inputType,
    )
