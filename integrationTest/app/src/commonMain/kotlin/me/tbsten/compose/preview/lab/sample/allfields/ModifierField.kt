package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.PreviewLab
import me.tbsten.compose.preview.lab.field.ModifierField
import me.tbsten.compose.preview.lab.field.mark
import me.tbsten.compose.preview.lab.field.modifier.ModifierFieldValue
import org.jetbrains.compose.ui.tooling.preview.Preview

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
