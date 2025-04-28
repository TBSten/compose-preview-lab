package me.tbsten.compose.preview.lab.sample.lib

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun MyButton(
    text: String,
) {
    Button(onClick = {}) {
        Text(text = text)
    }
}

@Preview
@Composable
private fun MyButtonPreview() {
    MyButton(text = "Hello MyButton")
}
