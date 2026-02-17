package me.tbsten.compose.preview.lab.sample

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.field.StringField
import me.tbsten.compose.preview.lab.field.transform
import me.tbsten.compose.preview.lab.previewlab.inspectorspane.InspectorTab
import me.tbsten.compose.preview.lab.sample.component.SamplePreviewLab
import me.tbsten.compose.preview.lab.sample.component.rememberCodeTab
import androidx.compose.ui.tooling.preview.Preview

@Preview
@ComposePreviewLabOption(id = "FieldTransform")
@Composable
private fun FieldTransform() = SamplePreviewLab(
    inspectorTabs = InspectorTab.defaults +
        listOf(
            rememberCodeTab(
                code = """
                fieldValue {
                    StringField("numberString", "0")
                        .transform(
                            transform = { it.toIntOrNull() ?: 0 },
                            reverse = { it.toString() },
                        )
                }
                """.trimIndent(),
            ),
        ),
) {
    val numberString: Int? =
        fieldValue {
            StringField("numberString", "0")
                .transform(
                    transform = { it.toIntOrNull() },
                    reverse = { it.toString() },
                )
        }

    Column(Modifier.fillMaxSize()) {
        Text("numberString: $numberString")
        Text("numberString::class: ${if (numberString != null) numberString::class.simpleName else "null"}")
    }
}
