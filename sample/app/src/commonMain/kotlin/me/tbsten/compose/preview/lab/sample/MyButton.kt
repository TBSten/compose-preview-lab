package me.tbsten.compose.preview.lab.sample

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import me.tbsten.compose.preview.lab.me.PreviewLab
import me.tbsten.compose.preview.lab.me.field.StringField
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun MyButton(
    text: String,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
    ) {
        Text(text = text)
    }
}

@Preview
@Composable
private fun MyButtonPreview() = PreviewLab {
    MyButton(
        text = fieldValue { StringField("MyButton.text", "Click Me") },
        onClick = { TODO("onEvent()") }
    )
}

@Preview
@Composable
private fun MyButtonPreview2() = PreviewLab {
    MyButton(
        text = fieldValue { StringField("MyButton.text", "Print test") },
        onClick = { println("😃 test !") }
    )
}
