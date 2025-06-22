package me.tbsten.compose.preview.lab.field

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.tbsten.compose.preview.lab.field.NumberField.InputType

/**
 * Field that holds a compose's Dp value.
 * In reality, it is almost a FloatField.
 *
 * ```kt
 * PreviewLab {
 *   MyButton(
 *     modifier = Modifier.size(fieldValue { DpField("size", 100.dp) })
 *   )
 * }
 * ```
 *
 * @param label label of the field.
 * @param initialValue initial value of the field.
 *
 * @see FloatField
 */
class DpField(label: String, initialValue: Dp) :
    TransformField<Float, Dp>(
        baseField = FloatField(
            label = label,
            initialValue = initialValue.value,
            inputType = InputType.TextField(
                suffix = { Text("dp") },
            ),
        ),
        transform = { it.dp },
        reverse = { it.value },
    )

/**
 * Field that holds a compose's Sp value.
 * In reality, it is almost a FloatField.
 *
 * ```kt
 * PreviewLab {
 *   Text(
 *     ...,
 *     fontSize = fieldValue { SpField("size", 20.sp) },
 *   )
 * }
 * ```
 *
 * @param label label of the field.
 * @param initialValue initial value of the field.
 *
 * @see FloatField
 */
class SpField(label: String, initialValue: TextUnit) :
    TransformField<Float, TextUnit>(
        baseField = FloatField(
            label = label,
            initialValue = initialValue.value,
            inputType = InputType.TextField(
                suffix = { Text("sp") },
            ),
        ),
        transform = { it.sp },
        reverse = { it.value },
    )

/**
 * Field that holds a compose's Offset value.
 *
 * @param label label of the field.
 * @param initialValue initial value of the field.
 *
 * @see MutablePreviewLabField
 */
class OffsetField(label: String, initialValue: Offset) :
    MutablePreviewLabField<Offset>(
        label = label,
        initialValue = initialValue,
    ) {
    @Composable
    override fun Content() {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            TextFieldContent(
                toString = { it.x.toString() },
                toValue = { runCatching { Offset(x = it.toFloat(), y = value.y) } },
                placeholder = { Text("x") },
                modifier = Modifier.weight(1f),
            )

            TextFieldContent(
                toString = { it.y.toString() },
                toValue = { runCatching { Offset(x = value.x, y = it.toFloat()) } },
                placeholder = { Text("y") },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * Field that holds a compose's DpOffset value.
 *
 * @param label label of the field.
 * @param initialValue initial value of the field.
 *
 * @see MutablePreviewLabField
 */
class DpOffsetField(label: String, initialValue: DpOffset) :
    MutablePreviewLabField<DpOffset>(
        label = label,
        initialValue = initialValue,
    ) {
    @Composable
    override fun Content() {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            TextFieldContent(
                toString = { it.x.value.toString() },
                toValue = { runCatching { DpOffset(x = it.toFloat().dp, y = value.y) } },
                placeholder = { Text("x") },
                modifier = Modifier.weight(1f),
            )

            TextFieldContent(
                toString = { it.y.value.toString() },
                placeholder = { Text("y") },
                toValue = { runCatching { DpOffset(x = value.x, y = it.toFloat().dp) } },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * Field that holds a compose's Size value.
 *
 * @param label label of the field.
 * @param initialValue initial value of the field.
 *
 * @see MutablePreviewLabField
 */
class SizeField(label: String, initialValue: Size) :
    MutablePreviewLabField<Size>(
        label = label,
        initialValue = initialValue,
    ) {
    @Composable
    override fun Content() {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            TextFieldContent(
                toString = { it.width.toString() },
                toValue = { runCatching { Size(width = it.toFloat(), height = value.height) } },
                placeholder = { Text("width") },
                modifier = Modifier.weight(1f),
            )

            TextFieldContent(
                toString = { it.height.toString() },
                toValue = { runCatching { Size(width = value.width, height = it.toFloat()) } },
                placeholder = { Text("height") },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * Field that holds a compose's DpSize value.
 *
 * @param label label of the field.
 * @param initialValue initial value of the field.
 *
 * @see MutablePreviewLabField
 */
class DpSizeField(label: String, initialValue: DpSize) :
    MutablePreviewLabField<DpSize>(
        label = label,
        initialValue = initialValue,
    ) {
    @Composable
    override fun Content() {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            TextFieldContent(
                toString = { it.width.value.toString() },
                toValue = {
                    runCatching {
                        DpSize(
                            width = it.toFloat().dp,
                            height = value.height,
                        )
                    }
                },
                placeholder = { Text("width") },
                modifier = Modifier.weight(1f),
            )

            TextFieldContent(
                toString = { it.height.value.toString() },
                toValue = { runCatching { DpSize(width = value.width, height = it.toFloat().dp) } },
                placeholder = { Text("height") },
                modifier = Modifier.weight(1f),
            )
        }
    }
}
