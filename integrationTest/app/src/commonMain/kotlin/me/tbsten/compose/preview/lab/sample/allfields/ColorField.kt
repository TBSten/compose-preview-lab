package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.PreviewLab
import me.tbsten.compose.preview.lab.field.ColorField
import me.tbsten.compose.preview.lab.field.withHint
import me.tbsten.compose.preview.lab.field.withPredefinedColorHint
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@ComposePreviewLabOption(id = "ColorFieldExample")
@Composable
internal fun ColorFieldExample() = PreviewLab {
    Box(
        modifier = Modifier
            .size(100.dp)
            .background(fieldValue { ColorField("Background", Color.Blue).withPredefinedColorHint() })
    )
}
