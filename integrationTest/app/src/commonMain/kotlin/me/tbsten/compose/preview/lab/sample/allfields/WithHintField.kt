package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.PreviewLab
import me.tbsten.compose.preview.lab.field.SpField
import me.tbsten.compose.preview.lab.field.withHint
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@ComposePreviewLabOption(id = "WithHintFieldExample")
@Composable
internal fun WithHintFieldExample() = PreviewLab {
    val fontSize = fieldValue {
        SpField(label = "Font Size", initialValue = 16.sp)
            .withHint(
                "Small" to 12.sp,
                "Medium" to 16.sp,
                "Large" to 20.sp,
                "XLarge" to 24.sp
            )
    }

    Text(
        text = "Sample Text",
        fontSize = fontSize,
    )
}
