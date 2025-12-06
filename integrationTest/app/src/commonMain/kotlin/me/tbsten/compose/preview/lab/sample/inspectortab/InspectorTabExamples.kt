package me.tbsten.compose.preview.lab.sample.inspectortab

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.PreviewLab
import me.tbsten.compose.preview.lab.component.inspectorspane.InspectorTab
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
    inspectorTabs = InspectorTab.defaults + listOf(DocsTab)
) {
    val text = fieldValue { StringField("text", "Hello") }
    Button(onClick = {}) {
        Text(text)
    }
}

// Usage Tab
internal object UsageTab : InspectorTab {
    override val title = "Usage"
    override val icon: (@Composable () -> Painter)? = null

    @Composable
    override fun InspectorTab.ContentContext.Content() {
        Column {
            Text("You can show sample code and usage examples here.")
        }
    }
}

@Preview
@ComposePreviewLabOption(id = "InspectorTabUsageExample")
@Composable
internal fun InspectorTabUsageExample() = PreviewLab(
    inspectorTabs = InspectorTab.defaults + listOf(UsageTab)
) {
    val text = fieldValue { StringField("text", "Hello") }
    Button(onClick = {}) {
        Text(text)
    }
}

// Design Tab
internal object DesignTab : InspectorTab {
    override val title = "Design"
    override val icon: (@Composable () -> Painter)? = null

    @Composable
    override fun InspectorTab.ContentContext.Content() {
        Column {
            Text("You can describe design guidelines here, such as color schemes and layout rules.")
        }
    }
}

@Preview
@ComposePreviewLabOption(id = "InspectorTabDesignExample")
@Composable
internal fun InspectorTabDesignExample() = PreviewLab(
    inspectorTabs = InspectorTab.defaults + listOf(DesignTab)
) {
    val text = fieldValue { StringField("text", "Hello") }
    Button(onClick = {}) {
        Text(text)
    }
}

// Debug Tab
internal object DebugTab : InspectorTab {
    override val title = "Debug"
    override val icon: (@Composable () -> Painter)? = null

    @Composable
    override fun InspectorTab.ContentContext.Content() {
        state.fields
        Column {
            Text("You can display debug information, logs, and state here.")
        }
    }
}

@Preview
@ComposePreviewLabOption(id = "InspectorTabDebugExample")
@Composable
internal fun InspectorTabDebugExample() = PreviewLab(
    inspectorTabs = InspectorTab.defaults + listOf(DebugTab)
) {
    val text = fieldValue { StringField("text", "Hello") }
    Button(onClick = {}) {
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
