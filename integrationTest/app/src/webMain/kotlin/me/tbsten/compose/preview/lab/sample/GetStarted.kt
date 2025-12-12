package me.tbsten.compose.preview.lab.sample

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.field.BooleanField
import me.tbsten.compose.preview.lab.field.ColorField
import me.tbsten.compose.preview.lab.field.ModifierField
import me.tbsten.compose.preview.lab.field.StringField
import me.tbsten.compose.preview.lab.field.modifier.ModifierFieldValue
import me.tbsten.compose.preview.lab.field.withHint
import me.tbsten.compose.preview.lab.previewlab.inspectorspane.InspectorTab
import me.tbsten.compose.preview.lab.sample.component.MyButton
import me.tbsten.compose.preview.lab.sample.component.previewLab
import me.tbsten.compose.preview.lab.sample.component.rememberCodeTab
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@ComposePreviewLabOption(id = "GetStarted")
@Composable
private fun GetStartedPreview() = previewLab(
    inspectorTabs = InspectorTab.defaults +
        listOf(
            rememberCodeTab(
                code = """
                PreviewLab {
                    val initialButtonContainerColor = MaterialTheme.colorScheme.primary
                    MyButton(
                        text = fieldValue {
                            StringField("text", "Click Me !")
                                .withHint(
                                    "Empty text" to "",
                                    "Long text" to "Very ".repeat(50) + "long text",
                                    "Three-line string" to "This\nis\na button.",
                                )
                        },
                        onClick = { onEvent("onClick") },
                        containerColor = fieldValue { ColorField("containerColor", initialButtonContainerColor) },
                        modifier = fieldValue { ModifierField("modifier", ModifierFieldValue) },
                        enabled = fieldValue { BooleanField("enabled", true) },
                    )
                }
                """.trimIndent(),
            ),
        ),
) {
    val initialButtonContainerColor = MaterialTheme.colorScheme.primary

    MyButton(
        text = fieldValue {
            StringField("text", "Click Me !")
                .withHint(
                    "Empty text" to "",
                    "Long text" to "Very ".repeat(50) + "long text",
                    "Three-line string" to "This\nis\na button.",
                )
        },
        onClick = { onEvent("onClick") },
        containerColor = fieldValue { ColorField("containerColor", initialButtonContainerColor) },
        modifier = fieldValue { ModifierField("modifier", ModifierFieldValue) },
        enabled = fieldValue { BooleanField("enabled", true) },
    )
}
