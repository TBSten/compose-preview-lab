package me.tbsten.compose.preview.lab.sample

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.component.inspectorspane.InspectorTab
import me.tbsten.compose.preview.lab.field.BooleanField
import me.tbsten.compose.preview.lab.field.CombinedField3
import me.tbsten.compose.preview.lab.field.StringField
import me.tbsten.compose.preview.lab.field.splitedOf
import me.tbsten.compose.preview.lab.sample.component.previewLab
import me.tbsten.compose.preview.lab.sample.component.rememberCodeTab
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@ComposePreviewLabOption(id = "FieldCombined")
@Composable
private fun FieldCombined() = previewLab(
    inspectorTabs = InspectorTab.defaults + listOf(
        rememberCodeTab(
            code = """
                fieldValue {
                    CombinedField3(
                        label = "uiState",
                        field1 = StringField("title", "..."),
                        field2 = StringField(
                            "description",
                            "...",
                        ),
                        field3 = BooleanField("isLoading", ...),
                        combine = { title, description, isLoading -> MyUiState(title, description, isLoading) },
                        split = { splitedOf(it.title, it.description, it.isLoading) },
                    )
                }
            """.trimIndent(),
        ),
    ),
) {
    val uiState = fieldValue {
        CombinedField3(
            label = "uiState",
            field1 = StringField("title", "CombinedField sample"),
            field2 = StringField(
                "description",
                "This is a CombinedField sample.\nMultiple fields can be combined and treated as a single field.",
            ),
            field3 = BooleanField("isLoading", false),
            combine = { title, description, isLoading -> MyUiState(title, description, isLoading) },
            split = { splitedOf(it.title, it.description, it.isLoading) },
        )
    }

    Scaffold { paddingValues ->
        Column(Modifier.padding(paddingValues).padding(20.dp)) {
            Text(uiState.title, style = MaterialTheme.typography.headlineMedium)
            Text(uiState.description, style = MaterialTheme.typography.bodyMedium)

            if (uiState.isLoading) {
                CircularProgressIndicator()
            }
        }
    }
}

data class MyUiState(val title: String, val description: String, val isLoading: Boolean)
