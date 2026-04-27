package me.tbsten.compose.preview.lab.sample.libreversed

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import me.tbsten.compose.preview.lab.field.StringField
import me.tbsten.compose.preview.lab.previewlab.PreviewLab

@Composable
fun MyButton(label: String, onClick: () -> Unit) {
    Button(onClick = onClick) {
        Text(label)
    }
}

@Preview
@Composable
fun MyButtonPreview() = PreviewLab {
    MyButton(
        label = fieldValue { StringField("Label", "Click me") },
        onClick = {},
    )
}
