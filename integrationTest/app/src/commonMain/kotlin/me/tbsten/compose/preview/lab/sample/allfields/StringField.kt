package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.PreviewLab
import me.tbsten.compose.preview.lab.field.StringField
import me.tbsten.compose.preview.lab.field.withTextHint
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@ComposePreviewLabOption(id = "StringFieldExample")
@Composable
internal fun StringFieldExample() = PreviewLab {
    StringFieldMyButton(
        text = fieldValue { StringField("text", "Click Me") },
    )
}

@Preview
@ComposePreviewLabOption(id = "StringFieldWithPrefixSuffixExample")
@Composable
internal fun StringFieldWithPrefixSuffixExample() = PreviewLab {
    StringFieldMyButton(
        text = fieldValue {
            StringField(
                label = "text",
                initialValue = "Click Me",
                prefix = { Text("$") },
                suffix = { Text("USD") },
            )
        },
    )
}

@Preview
@ComposePreviewLabOption(id = "StringFieldWithTextHintExample")
@Composable
internal fun StringFieldWithTextHintExample() = PreviewLab {
    val description = fieldValue {
        StringField("description", "Short")
            .withTextHint()
    }
    Text(description)
}

@Composable
internal fun StringFieldMyButton(text: String) {
    Button(onClick = {}) {
        Text(text)
    }
}
