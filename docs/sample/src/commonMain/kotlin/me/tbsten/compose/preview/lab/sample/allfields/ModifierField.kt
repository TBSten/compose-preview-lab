package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.previewlab.PreviewLab
import me.tbsten.compose.preview.lab.field.ModifierField
import me.tbsten.compose.preview.lab.field.mark
import me.tbsten.compose.preview.lab.field.modifier.ModifierFieldValue
import androidx.compose.ui.tooling.preview.Preview

/**
 * Demonstrates [ModifierField] for dynamically adjusting Modifier properties.
 *
 * Modify padding, size, background color, and other Modifier attributes
 * to see real-time changes on the button.
 */
@Preview
@ComposePreviewLabOption(id = "ModifierFieldExample")
@Composable
internal fun ModifierFieldExample() = PreviewLab {
    Button(
        onClick = { },
        modifier = fieldValue { ModifierField("Button modifier") },
    ) {
        Text("Styled Button")
    }
}

/**
 * Demonstrates [ModifierField] with [ModifierFieldValue.mark] initial value.
 *
 * The mark mode highlights the target element with a visual indicator,
 * making it easy to identify which component the modifier applies to.
 */
@Preview
@ComposePreviewLabOption(id = "ModifierFieldWithMarkExample")
@Composable
internal fun ModifierFieldWithMarkExample() = PreviewLab {
    Button(
        onClick = { },
        modifier = fieldValue {
            ModifierField(
                label = "Button modifier",
                initialValue = ModifierFieldValue.mark(),
            )
        },
    ) {
        Text("Styled Button")
    }
}
