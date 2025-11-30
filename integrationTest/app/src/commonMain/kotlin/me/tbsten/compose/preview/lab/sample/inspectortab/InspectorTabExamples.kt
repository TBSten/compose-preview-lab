package me.tbsten.compose.preview.lab.sample.inspectortab

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
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
            Text("このコンポーネントの詳細なドキュメントをここに表示できます。")
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
            Text("サンプルコードや利用例をここに掲載します。")
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
            Text("デザインガイドラインをここに記載します。配色やレイアウトルールなど。")
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
        Column {
            Text("デバッグ用の情報やログ、状態などを表示します。")
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
