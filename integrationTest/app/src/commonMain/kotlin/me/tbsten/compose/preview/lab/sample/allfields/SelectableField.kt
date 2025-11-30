package me.tbsten.compose.preview.lab.sample.allfields

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
import me.tbsten.compose.preview.lab.PreviewLab
import me.tbsten.compose.preview.lab.field.SelectableField
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@ComposePreviewLabOption(id = "SelectableFieldExample")
@Composable
internal fun SelectableFieldExample() = PreviewLab {
    val theme = fieldValue {
        SelectableField(
            label = "Theme",
            choices = listOf("Light", "Dark", "Auto")
        )
    }

    SelectableFieldMyApp(theme = theme)
}

@Preview
@ComposePreviewLabOption(id = "SelectableFieldChipsExample")
@Composable
internal fun SelectableFieldChipsExample() = PreviewLab {
    val theme = fieldValue {
        SelectableField(
            label = "Theme",
            choices = listOf("Light", "Dark", "Auto"),
            type = SelectableField.Type.CHIPS
        )
    }

    SelectableFieldMyApp(theme = theme)
}

@Preview
@ComposePreviewLabOption(id = "SelectableFieldMapExample")
@Composable
internal fun SelectableFieldMapExample() = PreviewLab {
    val theme = fieldValue {
        SelectableField(
            label = "Theme",
            choices = mapOf(
                "Light Mode" to "Light",
                "Dark Mode" to "Dark",
                "Auto (System)" to "Auto"
            )
        )
    }

    SelectableFieldMyApp(theme = theme)
}

@Preview
@ComposePreviewLabOption(id = "SelectableFieldBuilderExample")
@Composable
internal fun SelectableFieldBuilderExample() = PreviewLab {
    val theme = fieldValue {
        SelectableField<String>(label = "Theme") {
            choice("Light", label = "Light Mode", isDefault = true)
            choice("Dark", label = "Dark Mode")
            choice("Auto", label = "Auto (System)")
        }
    }

    SelectableFieldMyApp(theme = theme)
}

@Composable
internal fun SelectableFieldMyApp(theme: String) {
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
