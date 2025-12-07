package me.tbsten.compose.preview.lab.sample.inspectortab

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.PreviewLab
import me.tbsten.compose.preview.lab.component.inspectorspane.InspectorTab
import me.tbsten.compose.preview.lab.field
import me.tbsten.compose.preview.lab.field.DpSizeField
import me.tbsten.compose.preview.lab.field.MutablePreviewLabField
import me.tbsten.compose.preview.lab.field.StringField
import org.jetbrains.compose.ui.tooling.preview.Preview

// Docs Tab
internal object DocsTab : InspectorTab {
    override val title = "Docs"
    override val icon: (@Composable () -> Painter)? = null

    @Composable
    override fun InspectorTab.ContentContext.Content() {
        Column {
            Text("You can display detailed documentation for this component here.")
        }
    }
}

@Preview
@ComposePreviewLabOption(id = "InspectorTabDocsExample")
@Composable
internal fun InspectorTabDocsExample() = PreviewLab(
    inspectorTabs = InspectorTab.defaults + DocsTab,
) {
    LaunchedEffect(Unit) {
        delay(0.1.seconds)
        @OptIn(ExperimentalComposePreviewLabApi::class)
        state.selectedTabIndex = 2
    }

    val text = fieldValue { StringField("text", "Hello") }
    Button(onClick = {}) {
        Text(text)
    }
}

// Debug Tab
internal object DebugTab : InspectorTab {
    override val title = "Debug"
    override val icon: (@Composable () -> Painter)? = null

    @OptIn(ExperimentalComposePreviewLabApi::class)
    @Composable
    override fun InspectorTab.ContentContext.Content() {
        Column(modifier = Modifier.padding(8.dp)) {
            val allFields = state.fields
            Text("Field count: ${allFields.size}")

            Button(
                onClick = {
                    val textField: MutablePreviewLabField<String> by state.field<String>(label = "text")
                    val sizeField: MutablePreviewLabField<DpSize> by state.field<DpSize>(label = "size")

                    textField.value = "very ".repeat(50) + "text"
                    sizeField.value = DpSize(300.dp, 300.dp)
                },
            ) {
                Text("Set field value to large content pattern")
            }
        }
    }
}

@Preview
@ComposePreviewLabOption(id = "InspectorTabDebugExample")
@Composable
internal fun InspectorTabDebugExample() = PreviewLab(
    inspectorTabs = InspectorTab.defaults + DebugTab,
) {
    LaunchedEffect(Unit) {
        delay(0.1.seconds)
        @OptIn(ExperimentalComposePreviewLabApi::class)
        state.selectedTabIndex = 2
    }

    val text = fieldValue { StringField("text", "Hello") }
    Button(
        onClick = {},
        modifier = Modifier
            .size(fieldValue { DpSizeField("size", DpSize(100.dp, 80.dp)) }),
    ) {
        Text(text)
    }
}

// Code Tab
@Preview
@ComposePreviewLabOption(id = "InspectorTabCodeExample")
@Composable
internal fun InspectorTabCodeExample() = PreviewLab(
    inspectorTabs = InspectorTab.defaults + @OptIn(ExperimentalComposePreviewLabApi::class) InspectorTab.Code,
) {
    val text = fieldValue { StringField("text", "Hello") }
    Button(onClick = {}) {
        Text(text)
    }
}
