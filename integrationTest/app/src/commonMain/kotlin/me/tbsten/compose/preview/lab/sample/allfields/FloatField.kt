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
import me.tbsten.compose.preview.lab.field.FloatField
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@ComposePreviewLabOption(id = "FloatFieldExample")
@Composable
internal fun FloatFieldExample() = PreviewLab {
    TransparentBox(
        alpha = fieldValue { FloatField("Alpha", 0.5f) },
    )
}

@Composable
internal fun TransparentBox(alpha: Float) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .background(Color.Blue.copy(alpha = alpha))
    )
}
