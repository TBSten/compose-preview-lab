package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.PreviewLab
import me.tbsten.compose.preview.lab.field.StringField
import me.tbsten.compose.preview.lab.field.transform
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@ComposePreviewLabOption(id = "TransformFieldExample")
@Composable
internal fun TransformFieldExample() = PreviewLab {
    val intValue = fieldValue {
        StringField("number", "42")
            .transform(
                transform = { it.toIntOrNull() ?: 0 },
                reverse = { it.toString() }
            )
    }

    Column {
        Text("intValue: $intValue")
        Text("intValue::class: ${intValue::class.simpleName}")
    }
}
