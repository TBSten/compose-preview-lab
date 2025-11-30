package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.PreviewLab
import me.tbsten.compose.preview.lab.field.EnumField
import org.jetbrains.compose.ui.tooling.preview.Preview

enum class ButtonVariant { Primary, Secondary, Tertiary }

@Preview
@ComposePreviewLabOption(id = "EnumFieldExample")
@Composable
internal fun EnumFieldExample() = PreviewLab {
    val variant = fieldValue {
        EnumField("Variant", ButtonVariant.Primary)
    }

    EnumFieldMyButton(variant = variant)
}

@Composable
internal fun EnumFieldMyButton(variant: ButtonVariant) {
    val colors = when (variant) {
        ButtonVariant.Primary -> ButtonDefaults.buttonColors()
        ButtonVariant.Secondary -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary
        )
        ButtonVariant.Tertiary -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onTertiary
        )
    }
    Button(onClick = {}, colors = colors) {
        Text(variant.name)
    }
}
