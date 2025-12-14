package me.tbsten.compose.preview.lab.field

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ImmutablePreviewLabField
import me.tbsten.compose.preview.lab.field.modifier.ModifierBuilder
import me.tbsten.compose.preview.lab.field.modifier.ModifierBuilderState
import me.tbsten.compose.preview.lab.field.modifier.ModifierFieldValue
import me.tbsten.compose.preview.lab.field.modifier.ModifierFieldValueList
import me.tbsten.compose.preview.lab.field.modifier.background
import me.tbsten.compose.preview.lab.field.modifier.border
import me.tbsten.compose.preview.lab.field.modifier.padding

/**
 * Interactive field for building and editing Compose Modifier chains
 *
 * Provides a visual interface for constructing complex Modifier chains through a builder pattern.
 * Users can add, remove, and configure individual modifier values like padding, background,
 * size constraints, etc. The field displays a real-time preview of the constructed modifier
 * and allows fine-tuning of parameters through dedicated UI controls.
 *
 * # Usage
 *
 * ```kotlin
 * // Basic modifier field with default marking
 * @Preview
 * @Composable
 * fun ButtonPreview() = PreviewLab {
 *     Button(
 *         onClick = { },
 *         modifier = fieldValue { ModifierField("Button modifier") },
 *     ) {
 *         Text("Styled Button")
 *     }
 * }
 *
 * // Custom initial modifier chain
 * @Preview
 * @Composable
 * fun CardPreview() = PreviewLab {
 *     val customModifier: Modifier = fieldValue {
 *         ModifierField(
 *             label = "Card Modifier",
 *             initialValue = ModifierFieldValue.mark()
 *                 .padding(16.dp)
 *                 .background(Color.Blue)
 *         )
 *     }
 *
 *     Card(modifier = customModifier) {
 *         Text("Custom Card")
 *     }
 * }
 *
 * // Pre-configured with multiple modifiers
 * @Preview
 * @Composable
 * fun BoxPreview() = PreviewLab {
 *     val containerModifier: Modifier = fieldValue {
 *         ModifierField(
 *             label = "Container",
 *             initialValue = ModifierFieldValue.mark()
 *                 .padding(all = 8.dp)
 *                 .background(Color.Gray.copy(alpha = 0.3f))
 *                 .border(width = 2.dp, color = Color.Black)
 *         )
 *     }
 *
 *     Box(modifier = containerModifier) {
 *         Text("Container Content")
 *     }
 * }
 * ```
 *
 * @param label Display label for the field in the inspector
 * @param initialValue Starting modifier chain configuration
 * @see ModifierFieldValue
 * @see ModifierFieldValueList
 */
class ModifierField(label: String, initialValue: ModifierFieldValueList = ModifierFieldValue.mark()) :
    ImmutablePreviewLabField<Modifier>(
        label = label,
        initialValue = Modifier,
    ) {
    private val builderState = ModifierBuilderState(initialValue)

    override var value: Modifier
        get() = builderState.values.createModifier()
        set(_) {
            error("ModifierField.setValue not supported.")
        }

    @Composable
    override fun Content() {
        ModifierBuilder(
            state = builderState,
        )
    }
}

/**
 * Adds visual marking to a ModifierFieldValueList with border and background.
 *
 * # Usage
 *
 * ```kotlin
 * // Basic marking with default red color
 * @Preview
 * @Composable
 * fun MarkedPreview() = PreviewLab {
 *     val modifier: Modifier = fieldValue {
 *         ModifierField(
 *             label = "Marked Box",
 *             initialValue = ModifierFieldValue.mark()
 *         )
 *     }
 *
 *     Box(modifier = modifier) {
 *         Text("Marked Content")
 *     }
 * }
 *
 * // Custom color marking
 * @Preview
 * @Composable
 * fun CustomMarkPreview() = PreviewLab {
 *     val modifier: Modifier = fieldValue {
 *         ModifierField(
 *             label = "Blue Marked",
 *             initialValue = ModifierFieldValue.mark(color = Color.Blue, borderWidth = 3.dp)
 *         )
 *     }
 *
 *     Text("Blue Marked Text", modifier = modifier)
 * }
 * ```
 *
 * @param color The color to use for both border and background (with adjusted alpha)
 * @param borderWidth The width of the border
 * @return A new ModifierFieldValueList with marking applied
 */
fun ModifierFieldValueList.mark(color: Color = Color.Red.copy(alpha = 0.5f), borderWidth: Dp = 2.dp) = mark(
    borderColor = color,
    backgroundColor = color.copy(alpha = color.alpha * 0.5f),
    borderWidth = borderWidth,
)

/**
 * Adds visual marking to a ModifierFieldValueList with customizable border and background colors.
 *
 * # Usage
 *
 * ```kotlin
 * // Separate border and background colors
 * @Preview
 * @Composable
 * fun CustomColorMarkPreview() = PreviewLab {
 *     val modifier: Modifier = fieldValue {
 *         ModifierField(
 *             label = "Two-Color Mark",
 *             initialValue = ModifierFieldValue.mark(
 *                 borderColor = Color.Red,
 *                 backgroundColor = Color.Yellow.copy(alpha = 0.3f),
 *                 borderWidth = 2.dp
 *             )
 *         )
 *     }
 *
 *     Text("Two-Color Marked", modifier = modifier)
 * }
 *
 * // Thick border marking
 * @Preview
 * @Composable
 * fun ThickBorderPreview() = PreviewLab {
 *     val modifier: Modifier = fieldValue {
 *         ModifierField(
 *             label = "Thick Border",
 *             initialValue = ModifierFieldValue.mark(
 *                 borderColor = Color.Green,
 *                 backgroundColor = Color.Green.copy(alpha = 0.2f),
 *                 borderWidth = 5.dp
 *             )
 *         )
 *     }
 *
 *     Box(modifier = modifier.size(100.dp))
 * }
 * ```
 *
 * @param borderColor The color for the border
 * @param backgroundColor The color for the background
 * @param borderWidth The width of the border
 * @return A new ModifierFieldValueList with marking applied
 */
fun ModifierFieldValueList.mark(
    borderColor: Color = Color.Red.copy(alpha = 0.75f),
    backgroundColor: Color = borderColor.copy(alpha = borderColor.alpha * 0.5f),
    borderWidth: Dp = 2.dp,
) = border(color = borderColor, width = borderWidth)
    .background(color = backgroundColor)
    .padding(all = borderWidth)
