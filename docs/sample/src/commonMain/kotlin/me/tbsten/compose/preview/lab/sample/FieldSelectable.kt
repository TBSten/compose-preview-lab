package me.tbsten.compose.preview.lab.sample

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.field.SelectableField
import me.tbsten.compose.preview.lab.sample.component.previewLab
import androidx.compose.ui.tooling.preview.Preview

@ComposePreviewLabOption(id = "FieldSelectable")
@Preview
@Composable
private fun FieldSelectable() = previewLab {
    val theme =
        fieldValue {
            SelectableField(
                label = "theme",
                choices = listOf("Light", "Dark", "Auto"),
            )
        }

    MaterialTheme(
        colorScheme = when (theme) {
            "Light" -> lightColorScheme()
            "Dark" -> darkColorScheme()
            else -> if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
        },
    ) {
        Scaffold {
            Column(Modifier.padding(it)) {
                Text("current theme: $theme")
                Button(onClick = {}) {
                    Text("Button")
                }
            }
        }
    }
}
