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
@ComposePreviewLabOption(id = "FieldBasic")
@Composable
private fun FieldBasic() = previewLab(
    inspectorTabs = InspectorTab.defaults +
        listOf(
            rememberCodeTab(
                code = """
                MyButton(
                    // highlight-next-line
                    text = fieldValue { StringField("text", "Click Me") },
                    enabled = fieldValue { BooleanField("enabled", true) },
                )
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
