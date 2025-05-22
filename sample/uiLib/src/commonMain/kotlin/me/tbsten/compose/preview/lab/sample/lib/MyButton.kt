package me.tbsten.compose.preview.lab.sample.lib

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.PreviewLab
import me.tbsten.compose.preview.lab.field.StringField
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun MyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(onClick = onClick, modifier = modifier) {
        Text(text = text)
    }
}

@ComposePreviewLabOption(displayName = "UI Component in library module Preview")
@Preview
@Composable
private fun MyButtonPreview() = PreviewLab {
    MyButton(
        text = fieldValue { StringField("MyButton.text", "Click Me") },
        onClick = { onEvent("MyButton.onClick") },
        modifier = Modifier.padding(20.dp),
    )
}
