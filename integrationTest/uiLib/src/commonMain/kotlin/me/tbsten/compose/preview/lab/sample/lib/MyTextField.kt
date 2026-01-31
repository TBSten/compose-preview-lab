package me.tbsten.compose.preview.lab.sample.lib

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.previewlab.PreviewLab
import me.tbsten.compose.preview.lab.event.ChangeEvent
import me.tbsten.compose.preview.lab.field.StringField
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun MyTextField(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
    )
}

@ComposePreviewLabOption(
    id = "MyTextFieldPreview",
    displayName = "Statefull UI Component",
)
@Preview
@Composable
private fun MyTextFieldPreview() = PreviewLab {
    var textFieldValue by fieldState { StringField("MyTextField.value", "") }
        .also {
            ChangeEvent(
                value = it.value,
                title = { "MyTextField.value changed" },
                description = { "newValue: $it" },
            )
        }

    MyTextField(
        value = textFieldValue,
        onValueChange = { textFieldValue = it },
        modifier = Modifier.padding(20.dp),
    )
}
