package me.tbsten.compose.preview.lab.sample

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.field.StringField
import me.tbsten.compose.preview.lab.previewlab.inspectorspane.InspectorTab
import me.tbsten.compose.preview.lab.sample.component.MyTextField
import me.tbsten.compose.preview.lab.sample.component.SamplePreviewLab
import me.tbsten.compose.preview.lab.sample.component.rememberCodeTab
import androidx.compose.ui.tooling.preview.Preview

@ComposePreviewLabOption(id = "FieldState")
@Preview
@Composable
private fun FieldState() = SamplePreviewLab(
    inspectorTabs = InspectorTab.defaults +
        listOf(
            rememberCodeTab(
                code = """
            var text: String by fieldState { StringField("text", "Hello") }
            MyTextField(
                text = text,
                onTextChange = { text = it },
            )
                """.trimIndent(),
            ),
        ),
) {
    var text by fieldState { StringField("text", "My text field!") }

    MyTextField(
        value = text,
        onValueChange = { text = it },
    )
}
