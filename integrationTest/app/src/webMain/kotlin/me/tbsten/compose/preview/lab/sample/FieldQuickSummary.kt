package me.tbsten.compose.preview.lab.sample

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.field.BooleanField
import me.tbsten.compose.preview.lab.field.StringField
import me.tbsten.compose.preview.lab.previewlab.inspectorspane.InspectorTab
import me.tbsten.compose.preview.lab.sample.component.MyButton
import me.tbsten.compose.preview.lab.sample.component.previewLab
import me.tbsten.compose.preview.lab.sample.component.rememberCodeTab
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@ComposePreviewLabOption(id = "FieldQuickSummary")
@Composable
private fun FieldQuickSummaryPreview() = previewLab(
    inspectorTabs = InspectorTab.defaults +
        listOf(
            rememberCodeTab(
                code = """
            PreviewLab {
                MyButton(
                    // To use Field, call fieldValue and the Field class.
                    text = fieldValue { StringField("text", "Click Me !") },
                    enabled = fieldValue { BooleanField("enabled", true) },
                )
            }
                """.trimIndent(),
            ),
        ),
) {
    MyButton(
        text = fieldValue { StringField("text", "Click Me !") },
        enabled = fieldValue { BooleanField("enabled", true) },
        onClick = {},
    )
}
